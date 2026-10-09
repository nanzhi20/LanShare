package com.nanzhi.network.controller;

import com.nanzhi.network.model.LanAddress;
import com.nanzhi.network.service.LanAddressService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/network")
public class LanAddressController {
    private final LanAddressService addressService;

    public LanAddressController(LanAddressService addressService) {
        this.addressService = addressService;
    }

    @GetMapping("/addresses")
    public List<LanAddress> addresses() {
        return addressService.findAddresses();
    }
}
