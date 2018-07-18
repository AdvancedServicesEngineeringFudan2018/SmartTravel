package vesseldevA.restapi;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotMqttClient;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;
import vesseldevA.domain.Destination;
import vesseldevA.domain.Location;
import vesseldevA.domain.VesselState;
import vesseldevA.repos.*;
import vesseldevA.services.pubSub.VesselPublisher;
import vesseldevA.services.shadow.VesselDevice;
import vesseldevA.util.DateUtil;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SuppressWarnings("all")
public class APIController {
    private static final Logger logger = LoggerFactory.getLogger(APIController.class);
    private String stompUpdateShadowTopic = "/topic/vessel/shadow/update";

    @Autowired
    public SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    public CommonRepository commonRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private AWSClientService awsClientService;
    @Autowired
    private TrajectoryRepository trajectoryRepository;
    @Autowired
    private LocationRepository locationRepository;
    @Autowired
    private DestinationRepository destinationRepository;
    @Autowired
    private AsyncTaskService asyncTaskService;

    @RequestMapping("/hello")
    String home() {
        logger.info("test rest api.");
        return "hello , vessel-dev-A";
    }

    @RequestMapping(value = "/delay" , method = RequestMethod.POST , produces = "application/json")
//    public ResponseEntity<List<Destination>> delay(@RequestBody  List<Destination> newDestinations){
//        logger.debug("--delay--"+newDestinations.toString());
//        List<Destination> oldDestinations = vesselDevice.getDestinations();
//        for(int i = 0 ; i < newDestinations.size();i++){
//            Destination oldDest = oldDestinations.get(i);
//            Destination newDest = newDestinations.get(i);
//            oldDest.setEstiAnchorTime(newDest.getEstiAnchorTime());
//            oldDest.setEstiArrivalTime(newDest.getEstiArrivalTime());
//            oldDest.setEstiDepartureTime(newDest.getEstiDepartureTime());
//        }
//        return new ResponseEntity<List<Destination>>(newDestinations , HttpStatus.OK);
//    }

//    @RequestMapping(value = "/status/{status}" , method = RequestMethod.POST , produces = "application/json")
    public ResponseEntity<String> updateStatus(@PathVariable("status") String status){
        logger.debug("--delay--"+status);
//        vesselDevice.updateStatus(status);
        return new ResponseEntity<String>(status , HttpStatus.OK);
    }

    @RequestMapping(value = "/start" , method = RequestMethod.POST , produces = "application/json")
    public ResponseEntity<String> startVessel(@RequestBody Map<String, Object> mp) throws InterruptedException, AWSIotException, IOException {
        logger.debug("--/start--");
        Map<String , Object> vars = new HashMap<String , Object>();
        String vid = mp.get("vid").toString();
        int defaultDelayHour = Integer.parseInt(mp.get("defaultDelayHour").toString());
        int zoomInVal = Integer.parseInt(mp.get("zoomInVal").toString());
        commonRepository.setDefautDelayHour(defaultDelayHour);
        commonRepository.setZoomInVal(zoomInVal);
        asyncTaskService.initVesselState(vid);
        asyncTaskService.trackOnce(vid);
        return new ResponseEntity<String>(vid , HttpStatus.OK);
    }


}
