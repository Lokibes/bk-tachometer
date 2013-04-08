package vn.edu.hcmut.tachometer;

public class Profile {
    public String name, avatar;
    public int minRPM, maxRPM, numBlade;

    public Profile(String name, String avatar, int minRPM, int maxRPM, int numBlade) {
        this.name = name;
        this.avatar = avatar;
        this.minRPM = minRPM;
        this.maxRPM = maxRPM;
        this.numBlade = numBlade;
    }
    
    /** Template of a profile_<date>.xml file
     * 
     * <profile>
     * 		<name>Auto-created or user-input</name>
     * 		<avatar>Auto-created or user-input</avatar>
     * 		<minRPM>Minimum estimated velocity</minRPM>
     * 		<maxRPM>Maximum estimated velocity</maxRPM>
     * 		<numBlade>Number of blades</numBlade>
     * </profile>
     * 
     */
}