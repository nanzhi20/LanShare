package com.nanzhi.network;

import com.nanzhi.network.model.LanAddress;
import com.nanzhi.network.service.LanAddressService;
import java.util.List;

/** Run this main method in IDEA to inspect the addresses shown on the LanShare page. */
public class NetworkAddressManualCheck {
    public static void main(String[] args) {
        List<LanAddress> addresses = new LanAddressService().findAddresses();
        if (addresses.isEmpty()) {
            System.out.println("没有找到活动网卡的局域网 IPv4 地址。请检查网络连接。");
            return;
        }

        System.out.println("本机可用的局域网地址：");
        for (LanAddress address : addresses) {
            System.out.printf("%s -> http://%s:8080/%n", address.interfaceName(), address.ip());
        }
        System.out.println("如果应用使用的不是 8080 端口，请替换上面地址中的端口。");
    }
}
