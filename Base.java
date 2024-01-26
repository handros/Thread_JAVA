package concurent.student.first;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Base {

    private static final int STARTER_PEASANT_NUMBER = 5;
    private static final int PEASANT_NUMBER_GOAL = 10;

    // lock to ensure only one unit can be trained at one time
    private final ReentrantLock trainingLock = new ReentrantLock();

    private final String name;
    private final Resources resources = new Resources();
    private final List<Peasant> peasants = Collections.synchronizedList(new LinkedList<>());
    private final List<Building> buildings = Collections.synchronizedList(new LinkedList<>());

    public Base(String name){
        this.name = name;
        // TODO Create the initial 5 peasants - Use the STARTER_PEASANT_NUMBER constant
        // TODO 3 of them should mine gold
        // TODO 1 of them should cut tree
        // TODO 1 should do nothing
        // TODO Use the createPeasant() method
        
        for(int i=0; i<STARTER_PEASANT_NUMBER; i++) {
            peasants.add(createPeasant());
        }
        
        for (int i=0; i<3; i++) {
            Peasant p = peasants.get(i);
            new Thread(() -> {
                p.startMining();
            }).start();
        }
        
        new Thread(() -> {
            peasants.get(3).startCuttingWood();
        }).start();
        
        
    }

    public void startPreparation(){
        // TODO Start the building and training preparations on separate threads
        // TODO Tip: use the hasEnoughBuilding method

        // TODO Build 3 farms - use getFreePeasant() method to see if there is a peasant without any work

        // TODO Create remaining 5 peasants - Use the PEASANT_NUMBER_GOAL constant
        // TODO 5 of them should mine gold
        // TODO 2 of them should cut tree
        // TODO 3 of them should do nothing
        // TODO Use the createPeasant() method

        // TODO Build a lumbermill - use getFreePeasant() method to see if there is a peasant without any work

        // TODO Build a blacksmith - use getFreePeasant() method to see if there is a peasant without any work

        // TODO Wait for all the necessary preparations to finish

        // TODO Stop harvesting with the peasants once everything is ready
        
        new Thread(() -> {
            while(peasants.size() < PEASANT_NUMBER_GOAL) {
                if(peasants.size() < PEASANT_NUMBER_GOAL &&  resources.canTrain(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost, UnitType.PEASANT.foodCost)) {
                    if(peasants.size() == 6 || peasants.size() == 7) {
                        synchronized(peasants) {
                            createPeasant().startMining();
                        }
                    }
                    else if(peasants.size() == 8) {
                        synchronized(peasants) {
                            createPeasant().startCuttingWood();
                        }

                    }
                    else createPeasant();
                }
            }
        }).start();
        
        new Thread (()->{
            while(!hasEnoughBuilding(UnitType.FARM, 3)) {
                Peasant p = getFreePeasant();
                if(p != null) {
                    boolean b = p.tryBuilding(UnitType.FARM) ;
                    if(!b) {
                        Random random = new Random();
                        if(random.nextInt() % 2 == 0)
                            p.startMining();
                        else if(random.nextInt() % 5 == 0)
                            p.startCuttingWood();
                    }
                }
                else sleepForMsec(500);
            }
            while(!hasEnoughBuilding(UnitType.LUMBERMILL, 1)) {
                Peasant p = getFreePeasant();
                if(p != null) {
                    if(!p.tryBuilding(UnitType.LUMBERMILL)) {
                        Random random = new Random();
                        if(random.nextInt() % 2 == 0)
                            p.startMining();
                        else p.startCuttingWood();
                    }
                }
                else sleepForMsec(500);
            }
            while(!hasEnoughBuilding(UnitType.BLACKSMITH, 1)) {
                Peasant p = getFreePeasant();
                if(p != null) {
                    if(!p.tryBuilding(UnitType.BLACKSMITH)) {
                        Random rand = new Random();
                        if(rand.nextInt() % 2 == 0) {
                            p.startMining();
                        }
                        else p.startCuttingWood();
                    }
                }
                else sleepForMsec(500);
            }
        }).start();
        
        for(Peasant p : peasants) {
            p.stopHarvesting();
        }
        
        System.out.println(this.name + " finished creating a base");
        System.out.println(this.name + " peasants: " + this.peasants.size());
        for(Building b : buildings){
            System.out.println(this.name + " has a  " + b.getUnitType().toString());
        }

    }


    /**
     * Returns a peasants that is currently free.
     * Being free means that the peasant currently isn't harvesting or building.
     *
     * @return Peasant object, if found one, null if there isn't one
     */
    private Peasant getFreePeasant(){
        // TODO implement - use the peasant's isFree() method
        for(Peasant p : peasants) {
            if(p.isFree())
                return p;
        }
        return null;
    }

    /**
     * Creates a peasant.
     * A peasant could only be trained if there are sufficient
     * gold, wood and food for him to train.
     *
     * At one time only one Peasant can be trained.
     *
     * @return The newly created peasant if it could be trained, null otherwise
     */
    private Peasant createPeasant(){
        Peasant result;
        while(trainingLock.isLocked()) {
            Base.sleepForMsec(100);
        }
        if(resources.canTrain(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost, UnitType.PEASANT.foodCost)){

            // TODO 1: Sleep as long as it takes to create a peasant - use sleepForMsec() method
            // TODO 2: Remove costs
            // TODO 3: Update capacity
            // TODO 4: Use the Peasant class' createPeasant method to create the new Peasant

            // TODO Remember that at one time only one peasant can be trained
            // return result;
            
            
            trainingLock.lock();
            sleepForMsec(UnitType.PEASANT.buildTime);
            resources.removeCost(UnitType.PEASANT.goldCost, UnitType.PEASANT.woodCost);
            resources.updateCapacity(UnitType.PEASANT.foodCost);
            result = Peasant.createPeasant(this);
            peasants.add(result);
            trainingLock.unlock();

            return result;
        }
        return null;
    }

    public Resources getResources(){
        return this.resources;
    }

    public List<Building> getBuildings(){
        return this.buildings;
    }

    public String getName(){
        return this.name;
    }

    /**
     * Helper method to determine if a base has the required number of a certain building.
     *
     * @param unitType Type of the building
     * @param required Number of required amount
     * @return true, if required amount is reached (or surpassed), false otherwise
     */
    private boolean hasEnoughBuilding(UnitType unitType, int required){
        // TODO check in the buildings list if the type has reached the required amount
        int count = 0;
        for(Building b: buildings) {
            if(b.equals(unitType))
                count++;
        }
        return count < required;
    }

    private static void sleepForMsec(int sleepTime) {
        try {
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
    }

}
