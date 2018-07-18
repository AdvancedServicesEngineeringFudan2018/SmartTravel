package vesseldevA.websocket;

import com.amazonaws.services.iot.client.AWSIotException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import vesseldevA.domain.DeviceClient;
import vesseldevA.repos.AWSClientService;
import vesseldevA.repos.AsyncTaskService;
import vesseldevA.repos.CommonRepository;

import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class StompMessageController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public SimpMessagingTemplate simpMessagingTemplate;
    @Autowired
    private AWSClientService awsClientService;
    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    public CommonRepository commonRepository;

    @Autowired
    private ObjectMapper objectMapper;

    //Just for testing the communication of websocket
    @MessageMapping("/vessel/start")
    public void test(String str) throws InterruptedException, IOException, AWSIotException {
        System.out.println("***********/hello**********" + " : " + str);
        List<DeviceClient> deviceClientList = awsClientService.getAwsClients();
        for(int i = 0 ; i < deviceClientList.size();i++){
            String vid = deviceClientList.get(i).getVid();
            asyncTaskService.vesselSensor(vid);
        }
    }

    @MessageMapping("/vessel/monitor")
    public void monitor(String str) throws InterruptedException, IOException, AWSIotException {
        System.out.println("***********/hello**********" + " : " + str);
        logger.debug("--/start--");
        JsonNode rootNode = objectMapper.readTree(str);
        String vid = rootNode.findValue("vid").asText();
        int defaultDelayHour = rootNode.findValue("defaultDelayHour").asInt();
        int zoomInVal =rootNode.findValue("zoomInVal").asInt();
        commonRepository.setDefautDelayHour(defaultDelayHour);
        commonRepository.setZoomInVal(zoomInVal);
        asyncTaskService.collectData(vid);
    }
    @MessageMapping("/hello1")
    public void test1(String str , Principal principal) {
        System.out.println("***********/hello1**********" + " : " + str);
//        simpMessagingTemplate.convertAndSend("/topic/greetings" , "haha...");
        System.out.println(principal.getName());
        System.out.println("simpMessagingTemplate : " + simpMessagingTemplate);
        simpMessagingTemplate.convertAndSendToUser(principal.getName(), "/queue/greetings1", "haha...");
//        simpMessagingTemplate.convertAndSend("/queue/greetings", "haha...");
    }


}
