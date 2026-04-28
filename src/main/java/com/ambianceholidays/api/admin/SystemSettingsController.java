package com.ambianceholidays.api.admin;

import com.ambianceholidays.common.dto.ApiResponse;
import com.ambianceholidays.domain.settings.SystemSetting;
import com.ambianceholidays.domain.settings.SystemSettingRepository;
import com.ambianceholidays.exception.BusinessException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/settings")
public class SystemSettingsController {

    private final SystemSettingRepository settingRepo;

    public SystemSettingsController(SystemSettingRepository settingRepo) {
        this.settingRepo = settingRepo;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<List<SystemSetting>> list() {
        return ApiResponse.ok(settingRepo.findAll());
    }

    @GetMapping("/{key}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN','ADMIN_OPS')")
    public ApiResponse<SystemSetting> get(@PathVariable String key) {
        return ApiResponse.ok(settingRepo.findById(key)
                .orElseThrow(() -> BusinessException.notFound("Setting")));
    }

    @PutMapping("/{key}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ApiResponse<SystemSetting> update(@PathVariable String key,
            @RequestBody Map<String, String> body) {
        SystemSetting setting = settingRepo.findById(key)
                .orElseThrow(() -> BusinessException.notFound("Setting"));
        setting.setValue(body.getOrDefault("value", setting.getValue()));
        return ApiResponse.ok(settingRepo.save(setting));
    }
}
