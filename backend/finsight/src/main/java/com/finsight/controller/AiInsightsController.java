
package com.finsight.controller;

import com.finsight.service.AiInsightsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = {
        "http://localhost:3000",
        "http://localhost:5173"
})
public class AiInsightsController {

    private final AiInsightsService aiInsightsService;

    public AiInsightsController(AiInsightsService aiInsightsService) {
        this.aiInsightsService = aiInsightsService;
    }

    @PostMapping("/summary")
    public ResponseEntity<?> getSummary(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(aiInsightsService.getFinancialSummary(request));
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askAi(@RequestBody Map<String, Object> request) {
        return ResponseEntity.ok(aiInsightsService.askAi(request));
    }
}
