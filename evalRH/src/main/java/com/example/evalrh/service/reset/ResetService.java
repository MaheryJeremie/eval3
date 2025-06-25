package com.example.evalrh.service.reset;

import com.example.evalrh.service.FrappeService;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ResetService {
    private FrappeService frappeService;
    private final String RESET_ENDPOINT ="/method/erpnext.erpnext_integrations.doctype.reset_data.reset_data.vider_tables";

    public ResetService(FrappeService frappeService) {
        this.frappeService = frappeService;
    }
    public void reset() {
       frappeService.get(RESET_ENDPOINT,null);
    }
}
