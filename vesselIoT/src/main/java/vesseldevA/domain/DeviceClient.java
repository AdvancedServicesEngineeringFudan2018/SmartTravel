package vesseldevA.domain;

import com.amazonaws.services.iot.client.AWSIotMqttClient;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import vesseldevA.services.shadow.VesselDevice;
import vesseldevA.util.CsvUtil;

import java.util.List;

@Data
public class DeviceClient {
    private String vid;
    private VesselDevice vesselDevice;
    private AWSIotMqttClient awsIotMqttClient;
    private String awsUpdateShadowTopic;

}
