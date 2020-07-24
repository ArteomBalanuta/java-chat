package main.engine.server;

import java.util.ArrayList;
import java.util.List;

public class KeyVault {
    private List<String> whiteListedPublicKeys = new ArrayList<>();
    private List<String> blackListedPublicKeys = new ArrayList<>();

    public List<String> getBlackListedPublicKeys() {
        return blackListedPublicKeys;
    }

    public void setBlackListedPublicKeys(List<String> blackListedPublicKeys) {
        this.blackListedPublicKeys = blackListedPublicKeys;
    }

    public List<String> getWhiteListedPublicKeys() {
        return whiteListedPublicKeys;
    }

    public void setWhiteListedPublicKeys(List<String> whiteListedPublicKeys) {
        this.whiteListedPublicKeys = whiteListedPublicKeys;
    }
}
