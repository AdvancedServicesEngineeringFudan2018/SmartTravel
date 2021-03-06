package vesseldevA.repos;

import jxl.read.biff.BiffException;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import vesseldevA.domain.VesselState;
import vesseldevA.util.CsvUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Data
@Service
public class TrajectoryRepository {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private List<VesselState> vesselStates = new ArrayList<VesselState>();

    public TrajectoryRepository(@Value("${vessel.states.fileName}") String fileName) throws IOException, BiffException {
        logger.debug("----"+fileName);
        String dataPath = this.getClass().getResource("/").getPath()+"data/";
        vesselStates = CsvUtil.readTracjectory(dataPath+fileName);
        logger.debug("TrajectoryRepository");
    }

    public VesselState findVesselState(int pos){
        return  vesselStates.get(pos);
    }

    public int size(){
        return vesselStates.size();
    }
}
