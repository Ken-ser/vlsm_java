package com.vlsm;

import java.util.ArrayList;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {

        // user input for ip and sm
        System.out.println("===================================================");
        System.out.println("-Enter ip address: ");
        Scanner scanner = new Scanner(System.in);
        String starterIp = scanner.next();
        System.out.println("-Enter subnet mask: ");
        String starterSm = scanner.next();

        // user input for number of LANs and number of HOSTs for each LAN
        System.out.println("-Enter the number of LANs: ");
        int nLan = scanner.nextInt();
        System.out.println("===================================================");
        int[] hosts = new int[nLan];
        int[] originalOrder = new int[nLan];
        
        for (int i = 0; i < nLan; i++) {
            originalOrder[i] = i;
            System.out.println("-Hosts (network and broadcast IPs excluded) for LAN " +
                    (i + 1) + ":");
            hosts[i] = scanner.nextInt() + 2;
        }
        scanner.close();

        ipv4Address starterAddress = new ipv4Address(starterIp, starterSm);
        System.out.println("=================-Starter address-=================\n" + starterAddress.toString()
                + "\n=====================-Subnets-=====================");

        // orders hosts number array from max to min value
        int max;
        int temp;
        int shift = 0;
        while (shift <= hosts.length - 2) {
            max = shift;
            for (int i = shift; i < hosts.length - 1; i++) {
                if (hosts[i + 1] > hosts[max])
                    max = i + 1;
            }
            temp = hosts[shift];
            hosts[shift] = hosts[max];
            hosts[max] = temp;

            // saves the new LAN ID order due to the hosts sort
            temp = originalOrder[shift];
            originalOrder[shift] = originalOrder[max];
            originalOrder[max] = temp;

            shift++;
        }

        // calculates the subnets
        ArrayList<ipv4Address> subnets = new ArrayList<ipv4Address>();
        ipv4Address unassignedSubnet = new ipv4Address(starterAddress.getIp(), starterAddress.getSubnetMask());
        boolean found;
        for (int i : hosts) {
            found = false;
            // finds the host id
            int hostId = 0;
            while (!found) {
                if (Math.pow(2, hostId) >= i)
                    found = true;
                else
                    hostId++;
            }

            int netId = 32 - hostId;

            // adds subnet to array
            unassignedSubnet.setSubnetMask(netId);
            int[] subnetMask = unassignedSubnet.getSubnetMask();
            int[] ip = unassignedSubnet.getIp();
            subnets.add(subnets.size(), new ipv4Address(ip, subnetMask));

            // updates unassigned ip (network id for the next subnet)
            // calculates ip's sector to update, -1 because index starts from 0
            int sector = (netId / 8) - 1;
            if (netId % 8 != 0)
                sector++;

            int[] newIp = unassignedSubnet.getNetworkId();
            int newSectorValue = ((int) Math.pow(2, hostId % 8)) + newIp[sector];

            // if the network bits of the sector to update are all 1s -> sector--
            while (newSectorValue > subnetMask[sector]) {
                newIp[sector] = 0;
                sector--;
                newSectorValue = newIp[sector] + 1;
            }

            newIp[sector] = newSectorValue;
            unassignedSubnet.setIp(newIp);
        }

        // prints the subnets
        for (int i = 0; i < nLan; i++) {
            System.out.println("[LAN " + (originalOrder[i] + 1) + "]\n" + subnets.get(i).toString() + "\n");
        }
    }
}
