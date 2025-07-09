package jp.ac.tsukuba.eclab.koudounext.backend.backend;




import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import jp.ac.tsukuba.eclab.koudounext.core.engine.controller.SimulationController;
import jp.ac.tsukuba.eclab.koudounext.core.engine.exception.simulation.SimulationNotRunningException;
import jp.ac.tsukuba.eclab.koudounext.core.engine.utils.SimulationConfig;



@RestController
@RequestMapping ("/simulation")
public class SimulationREST {



private final SimulationController simulationController = SimulationController.getInstance();

 @PostMapping ("/start")
    public ResponseEntity <String> startSimulation(@RequestBody SimulationConfig config) {

         try {
            simulationController.init(config);
            simulationController.startSimulation();
            return ResponseEntity.ok("Simulation initialized and started successfully.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to start simulation: " + e.getMessage());
        }




    }  

    @PostMapping("/stop")
    public ResponseEntity <String> stopSimulation() {


        try {
        
            simulationController.stopSimulation();
            return ResponseEntity.ok("Simulation stopped successfully.");
        } catch (SimulationNotRunningException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("exception thrown");
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to stop simulation: " + e.getMessage());
        }
    }
    
}

