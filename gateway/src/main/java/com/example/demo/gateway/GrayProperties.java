package com.example.demo.gateway;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "gateway.gray")
public class GrayProperties {
    private boolean enabled = true;
    private String version = "v2";
    /** 命中灰度的用户白名单 */
    private List<String> uids = new ArrayList<>();

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public List<String> getUids() { return uids; }
    public void setUids(List<String> uids) { this.uids = uids; }
}
