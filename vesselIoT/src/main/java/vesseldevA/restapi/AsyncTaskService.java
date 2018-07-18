package vesseldevA.restapi;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import vesseldevA.domain.*;
import vesseldevA.repos.*;
import vesseldevA.services.pubSub.VesselPublisher;
import vesseldevA.services.shadow.VesselDevice;
import vesseldevA.util.DateUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@SuppressWarnings("all")
public class AsyncTaskService {
    private static final Logger logger = LoggerFactory.getLogger(AsyncTaskService.class);
    private String stompUpdateShadowTopic = "/topic/vessel/shadow/update";
    private String stompInitShadowTopic = "/topic/vessel/shadow/init";

    @Autowired
    public SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    public CommonRepository commonRepository;

    @Autowired
    private AWSClientService awsClientService;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TrajectoryRepository trajectoryRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private DestinationRepository destinationRepository;

    @Autowired
    private TrackService trackService;


    public void initVesselState(String vid) throws InterruptedException, IOException, AWSIotException {
        DeviceClient deviceClient = awsClientService.findDeviceClient(vid);
        String updateShadowTopic = deviceClient.getAwsUpdateShadowTopic();
        VesselDevice vesselDevice = deviceClient.getVesselDevice();

        objectMapper.setFilterProvider(new SimpleFilterProvider().addFilter("wantedProperties", SimpleBeanPropertyFilter
                .filterOutAllExcept("vesselState", "destinations", "positionIndex", "simuStartTime", "vid", "nextPortIndex", "status")));
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        Track track =  trackService.findTrack(vid);
        List<Step> steps = track.getSteps();

        vesselDevice.updatePositionIndex(0);
        vesselDevice.updateStatus("Initiating");
        vesselDevice.updateNextPortIndex(0);

        //when vessel just starts out , preprocess the port data.
        //update default delay time at each port
        long defaultDelayMs = commonRepository.getDefautDelayHour() * 60 * 60 * 1000;
        long simuStartMs = new Date().getTime();
        vesselDevice.updateSimuStartTime(DateUtil.ms2dateStr(simuStartMs));
        long rawStartMs = DateUtil.str2date(trajectoryRepository
                .findVesselState(vesselDevice.getPositionIndex())
                .getTimeStamp())
                .getTime();
        int i = 0;
        List<Destination> destinations = new ArrayList<Destination>();
        for(Step step : steps){
            Destination d = new Destination();
            String ts = step.getVesselStates().get(step.getVesselStates().size()-1).getTimeStamp();
            d.setName(step.getNextPort());
            d.setEstiAnchorTime(ts);
            d.setEstiArrivalTime(ts);
            long rawArrivalMs = DateUtil.str2date(d.getEstiArrivalTime()).getTime();
            long simuArrivalMs = simuStartMs + rawArrivalMs - rawStartMs;
            simuArrivalMs += i * defaultDelayMs;
            String simuArrivalStr = DateUtil.ms2dateStr(simuArrivalMs);
            long simuDepartureMs = simuArrivalMs + defaultDelayMs;
            String simuDepartureStr = DateUtil.ms2dateStr(simuDepartureMs);
            if (simuArrivalStr != null && simuDepartureStr != null) {
                d.setEstiAnchorTime(simuArrivalStr);
                d.setEstiArrivalTime(simuArrivalStr);
                d.setEstiDepartureTime(simuDepartureStr);
            } else {
                logger.error("exists the error in preprocessing the port data.");
            }
            destinations.add(d);
        }
        logger.info("first time to adjust destinations : " + destinations);
        vesselDevice.updateDestinations(destinations);
        VesselState initVesselState = trajectoryRepository.findVesselState(0).deepCopy();
        vesselDevice.updateVesselState(initVesselState);

        String payload = "{\"state\":{\"desired\":" + objectMapper.writeValueAsString(vesselDevice) + "}}";
        logger.debug("init--->payload : " + payload);
        AWSIotMessage initPub = new VesselPublisher(updateShadowTopic, AWSIotQos.QOS0, payload);
        deviceClient.getAwsIotMqttClient().publish(initPub);
    }

//    @Async
    public void reportStep(String vid) throws InterruptedException, AWSIotException, JsonProcessingException {
        DeviceClient deviceClient = awsClientService.findDeviceClient(vid);
        String updateShadowTopic = deviceClient.getAwsUpdateShadowTopic();
        VesselDevice vesselDevice = deviceClient.getVesselDevice();

        vesselDevice.updateStatus("Voyaging");

        Track track =  trackService.findTrack(vid);
        long zoomInVal = commonRepository.getZoomInVal();
        int curTrackIndex = vesselDevice.getNextPortIndex();
        Step curStep = track.getSteps().get(curTrackIndex);
        Destination curDest = vesselDevice.getDestinations().get(curTrackIndex);
        List<VesselState> stepVesselStates = curStep.getVesselStates();
        int pos = vesselDevice.getPositionIndex();
        int size = stepVesselStates.size();
        int i = 0;
        long x = 0;
        long y = 0;
        while(i < size){
            vesselDevice.updatePositionIndex(pos+1);
            x = System.currentTimeMillis();
            logger.info("<" + i + ">" + (x - y));
            VesselState curVesselState = stepVesselStates.get(i).deepCopy();// deep copy to avoid modifying the vesselStates map
            long sleepMs = 0;
            //TODO:calculate elapse time between current state and next state
            if (i < size - 1) {
                long curStateMs = DateUtil.str2date(stepVesselStates.get(i).getTimeStamp()).getTime();
                long nextStateMs = DateUtil.str2date(stepVesselStates.get(i+ 1).getTimeStamp()).getTime();
                sleepMs = nextStateMs - curStateMs;
                logger.info(stepVesselStates.get(i)+":"+stepVesselStates.get(i + 1)+":"+sleepMs+"sleep : " + sleepMs / zoomInVal);
            }
            //TODO: modify the date in vessel state.
            long curMs = new Date().getTime();
            long startMs = DateUtil.str2date(vesselDevice.getSimuStartTime()).getTime();
            String newStateTime = DateUtil.ms2dateStr(startMs + (curMs - startMs) * zoomInVal);
            curVesselState.setTimeStamp(newStateTime);
            logger.debug("Current date in vessel state : " + newStateTime);
            vesselDevice.updateVesselState(curVesselState);

            //TODO: sync vessel device data to vessel shadow
            String payload = "{\"state\":{\"desired\":" + objectMapper.writeValueAsString(vesselDevice) + "}}";
            AWSIotMessage pub = new VesselPublisher(updateShadowTopic, AWSIotQos.QOS0, payload);
            logger.debug("voyaging--->payload : " + payload);
            deviceClient.getAwsIotMqttClient().publish(pub);

            simpMessagingTemplate.convertAndSend(stompUpdateShadowTopic ,  vesselDevice);
            i++;
            y = x;
            Thread.sleep(sleepMs / zoomInVal);
        }

        if(curTrackIndex < track.getSteps().size()-1){
            Destination nextDest = vesselDevice.getDestinations().get(curTrackIndex+1);
            Location nextLoc = locationRepository.findLocation(nextDest.getName());
            //TODO: Determine if the status is  anchoring or docking
            ObjectNode payloadObjectNode = objectMapper.createObjectNode();
            if (nextDest.getEstiAnchorTime().equals(nextDest.getEstiArrivalTime())) {
                logger.info("Transiting into Docking status"+nextDest.toString());
                vesselDevice.updateStatus("Docking");
                payloadObjectNode.put("msgType", "DOCKING");
            } else {
                logger.info("Transiting into Anchoring status"+nextDest.toString());
                vesselDevice.updateStatus("Anchoring");
                payloadObjectNode.put("msgType", "ANCHORING");
            }
            logger.info("Current port : " + nextLoc);
            int nextTrackIndex = curTrackIndex+1;
            vesselDevice.updateNextPortIndex(nextTrackIndex);

            logger.info("Next port : " + nextDest);
            //TODO: sync vessel device data to vessel shadow
            String payload = "{\"state\":{\"desired\":" + objectMapper.writeValueAsString(vesselDevice) + "}}";
            AWSIotMessage pub = new VesselPublisher(updateShadowTopic, AWSIotQos.QOS0, payload);
            deviceClient.getAwsIotMqttClient().publish(pub);
            simpMessagingTemplate.convertAndSend(stompUpdateShadowTopic ,  vesselDevice);
//                changeStatus(changeStatusTopic , "VOYAGING_END" , vesselDevice);
            logger.info("reach , nextPortIndex = " + nextTrackIndex);
        }

        if (curTrackIndex == vesselDevice.getDestinations().size()-1) {
            logger.info("The vessel arrived at the last port.");
        }
    }


    public void simuAD(String vid){
        DeviceClient deviceClient = awsClientService.findDeviceClient(vid);
        VesselDevice vesselDevice = deviceClient.getVesselDevice();
        //TODO: Timing simulation of anchoring and docking status of the ship
        long zoomVal = commonRepository.getZoomInVal();
        long simuMs = DateUtil.str2date(vesselDevice.getSimuStartTime()).getTime();
        while (true) {
            long curMs = (new Date().getTime() - simuMs) * zoomVal + simuMs;
            long nextMs = curMs + 1000 * zoomVal;
            Destination curDest = vesselDevice.getDestinations().get(vesselDevice.getNextPortIndex() - 1);
            if (vesselDevice.getStatus().equals("Anchoring")) {
                long newReachMs = DateUtil.str2date(curDest.getEstiArrivalTime()).getTime();
                logger.debug("Current time : " + DateUtil.ms2dateStr(curMs) + " Next time : " + DateUtil.ms2dateStr(nextMs) + "new reach time : " + curDest.getEstiArrivalTime());
                if (newReachMs > curMs && newReachMs <= nextMs) {
                    vesselDevice.updateStatus("Docking");
//                            changeStatus(changeStatusTopic , "ANCHORING_END" , vesselDevice);
                }
            } else if (vesselDevice.getStatus().equals("Docking")) {
                long newDepartureMs = DateUtil.str2date(curDest.getEstiDepartureTime()).getTime();
                logger.info("Current time : " + DateUtil.ms2dateStr(curMs) + " Next time : " + DateUtil.ms2dateStr(nextMs) + " New arrival time : " + curDest.getEstiDepartureTime());
                if (newDepartureMs > curMs && newDepartureMs <= nextMs) {
                    //send depature message to vessel process
                    vesselDevice.updateStatus("Voyaging");
//                            changeStatus(changeStatusTopic , "DOCKING_END" , vesselDevice);
                    logger.info("Docking  , departure");
                    break;
                }
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Async
    public void trackOnce(String vid) throws InterruptedException, AWSIotException, JsonProcessingException {
        DeviceClient deviceClient = awsClientService.findDeviceClient(vid);
        VesselDevice vesselDevice = deviceClient.getVesselDevice();
        Track track =  trackService.findTrack(vid);
        int curTrackIndex = vesselDevice.getNextPortIndex();
        List<Destination> destinations = vesselDevice.getDestinations();
        int dSize = destinations.size();
        while(curTrackIndex < dSize){
            reportStep(vid);
        }
    }
    @Async
    public void testAsync() throws InterruptedException {
        for(int i = 0 ; i < 10; i++){
            Thread.sleep(1000);
            logger.debug("test async task");

        }
    }
    @Async
    public void testAsync1() throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            Thread.sleep(1000);
            logger.debug("test async task 1");

        }
    }
}
