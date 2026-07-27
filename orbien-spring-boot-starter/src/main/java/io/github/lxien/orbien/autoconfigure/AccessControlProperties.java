package io.github.lxien.orbien.autoconfigure;

import io.github.lxien.orbien.core.enums.AccessControl;
import lombok.Data;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Data
public class AccessControlProperties implements Serializable {

    private boolean enabled = false;

    private AccessControl mode = AccessControl.ALLOW;

    private Set<String> allow = new HashSet<>();

    private Set<String> deny = new HashSet<>();
}
