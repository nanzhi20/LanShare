package com.nanzhi.network.controller;

import com.nanzhi.network.model.LanAddress;
import com.nanzhi.network.exception.NetworkExceptions.InvalidNetworkAddressException;
import com.nanzhi.network.service.LanAddressService;
import com.nanzhi.network.service.QrCodeService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/network")
public class LanAddressController {
    private final LanAddressService addressService;
    private final QrCodeService qrCodeService;

    public LanAddressController(LanAddressService addressService, QrCodeService qrCodeService) {
        this.addressService = addressService;
        this.qrCodeService = qrCodeService;
    }

    @GetMapping("/addresses")
    public List<LanAddress> addresses() {
        return addressService.findAddresses();
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrCode(@RequestParam String ip, HttpServletRequest request) {
        boolean knownAddress = addressService.findAddresses().stream()
                .anyMatch(address -> address.ip().equals(ip));
        if (!knownAddress) {
            throw new InvalidNetworkAddressException("该地址不是当前可用的局域网地址");
        }

        String accessUrl = UriComponentsBuilder.newInstance()
                .scheme(request.getScheme())
                .host(ip)
                .port(request.getServerPort())
                .path("/")
                .build()
                .toUriString();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .contentType(MediaType.IMAGE_PNG)
                .body(qrCodeService.generate(accessUrl));
    }
}
