package vesseldevA.repos;

import jxl.read.biff.BiffException;
import lombok.Data;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vesseldevA.domain.Destination;
import vesseldevA.util.CsvUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Data
@Service
public class DestinationRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<Destination> destinations = new ArrayList<Destination>();

    public DestinationRepository(@Value("${vessel.destinations.fileName}") String destinationsCsv) throws IOException, BiffException {
        logger.debug(destinationsCsv+"--");
//        String path = this.getClass().getResource("/").getPath()+"data/"+destinationsCsv;
//        List<String> destinations = CsvUtil.readDestinations(path);
//        logger.debug("destinations : "+destinations.toString());
    }

    public Destination findDestination(String name){
        for(Destination destination : destinations){
            if(name.equals(destination.getName())){
                return destination;
            }
        }
        return null;
    }
}
