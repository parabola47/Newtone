package com.parabola.domain.exceptions;

import com.parabola.domain.repository.AccessRepository;

public class PermissionDeniedException extends Exception {

    public final AccessRepository.AccessType permissionType;

    public PermissionDeniedException(AccessRepository.AccessType permissionType) {
        super("Permission " + permissionType + " not granted");
        this.permissionType = permissionType;
    }
}
