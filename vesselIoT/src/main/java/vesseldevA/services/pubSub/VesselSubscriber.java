package vesseldevA.services.pubSub;

import com.amazonaws.services.iot.client.AWSIotException;
import com.amazonaws.services.iot.client.AWSIotMessage;
import com.amazonaws.services.iot.client.AWSIotQos;
import com.amazonaws.services.iot.client.AWSIotTopic;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import vesseldevA.repos.CommonRepository;
import vesseldevA.repos.TrajectoryRepository;
import vesseldevA.services.shadow.VesselDevice;

import java.io.IOException;

/**
 * This class extends {@link AWSIotTopic} to receive messages from a subscribed
 * topic.
 * Author : bqzhu
 */
@SuppressWarnings("all")
public class VesselSubscriber extends AWSIotTopic {
    private static Logger logger = Logger.getLogger(VesselSubscriber.class);

    @Autowired
    private CommonRepository commonRepository;
    @Autowired
    private TrajectoryRepository trajectoryRepository;
    @Autowired
    private VesselDevice vesselDevice;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    public VesselSubscriber(String topic, AWSIotQos qos) {
        super(topic, qos);
    }

    @Override
    public void onMessage(AWSIotMessage message) {
        //called when a message is received.
        String receivedTopic = message.getTopic();
//        logger.debug(receivedTopic);
        String vid = vesselDevice.getVid();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootJNode = null;
        try {
            rootJNode = objectMapper.readTree(message.getStringPayload());
        } catch (IOException e) {
            e.printStackTrace();
        }
        String msgType = rootJNode.findValue("msgType").asText();
        String from = rootJNode.findValue("from").asText();
        logger.debug("message :" + msgType + " from : " + from);
//        if (from.equals(vid)) {
//            try {
//                switch (receivedTopic) {
//                    case "$aws/things/vessel/shadow/update/accepted":
//                        vesselShadowForwarding(message);
//                        break;
//                    case "activiti/vessel/init":
//                        logger.info("received topic :" + message.getTopic());
//                        initHandler(rootJNode);
//                        break;
//                    case "activiti/vessel/voyaging":
//                        logger.info("received topic :" + message.getTopic());
//                        voyagingHandler(rootJNode);
//                        break;
//                    case "activiti/vessel/delay":
//                        logger.info("received topic :" + message.getTopic());
//                        delayHandler(rootJNode);
//                    default:
//                        break;
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            } catch (AWSIotException e) {
//                e.printStackTrace();
//            }
//
//        }

    }
}
