package pakAbm;

import java.util.ArrayList;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.util.ContextUtils;

public class Household {

	private ContinuousSpace<Object> space;
	private int size;
	private int migrationNetwork;

	
	public ArrayList<Person> householdNetwork = new ArrayList();
	public ArrayList<Person> migratedMembers = new ArrayList();

	
	
	//Constructor
	public Household(ContinuousSpace<Object> space, int size, int migrationNetwork) {
		
		this.space = space;
		this.size = size;
		this.migrationNetwork = migrationNetwork;
		
		
		
		//populate household based in size passed to it
		for (int i = 0; i < this.size; i++) {
			//add new person to ArrayList
			householdNetwork.add(new Person(this.space, getRandomAge()));
			
					
			
			//System.out.println(PakAbmBuilder.TEMP);
			//PakAbmBuilder.TEMP = PakAbmBuilder.TEMP + 1;
		}
		
		for (int i = 0; i < householdNetwork.size(); i++) {
			//set each person's migrationNetwork to the household network size
			householdNetwork.get(i).migNetworkSize = migrationNetwork;
			System.out.println(householdNetwork.get(i).migNetworkSize);
		}
	
	}
	
	@ScheduledMethod(start=1, interval=1, priority=26)
	public void updateAgents() {
		
		//change of new child being born
		Random birthSeed = new Random();
		if (birthSeed.nextInt(100) < PakAbmBuilder.BIRTH_RATE) {
			//System.out.println("somebody was born");
			householdNetwork.add(new Person(this.space, 0));
			Context<Object> context = ContextUtils.getContext(this);
			context.add(householdNetwork.get(householdNetwork.size() - 1));
		}
		
		
		
		//in migration
		Random inSeed = new Random();
		//Add a thing hereso that in migration only happens if the family has more than one person in their migration network
		
		if (inSeed.nextInt(100) < PakAbmBuilder.IN_MIGRATION) {
			householdNetwork.add(new Person(this.space, getRandomAge()));
			Context<Object> context = ContextUtils.getContext(this);
			context.add(householdNetwork.get(householdNetwork.size() - 1));
			System.out.println("in migration");
		}
		
		
		//deaths taking place
		Random deathSeed = new Random();
		Random deathIndexSeed = new Random();
		if (deathSeed.nextInt(100) < PakAbmBuilder.DEATH_RATE) {
			//System.out.println("death happened");
			Context<Object> context = ContextUtils.getContext(this);
			if(householdNetwork.size() > 0) {
				int deathIndex = deathIndexSeed.nextInt(householdNetwork.size());
				context.remove(householdNetwork.get(deathIndex));
				householdNetwork.remove(householdNetwork.get(deathIndex));
			}
		}
		
		
		int numMigrants = 0;
		//ArrayList<Integer> markMigrants = new ArrayList();
		//mark members for deletion (migration)
		for (int i = householdNetwork.size() - 1; i >= 0; i--) {
			if (householdNetwork.get(i).migFlag == true) {
				
				//count the number of adults at home
				int numAdults = 0;
				for (int f = 0; f < householdNetwork.size(); f++) {
					if (householdNetwork.get(f).age >= PakAbmBuilder.ADULT_AGE) {
						numAdults++;
					}
				}
				
				//if there are at least two adults at home, let the flagged migrant go
				if (numAdults > 1) {
					if (householdNetwork.get(i).migratedFlag == false) {
						numMigrants++;
					}
					householdNetwork.get(i).migFlag = false;
					Context<Object> context = ContextUtils.getContext(this);
					context.remove(householdNetwork.get(i));
					migratedMembers.add(householdNetwork.get(i));
					householdNetwork.remove(i);
					
					//add new migrants to replace old
					if (PakAbmBuilder.SPAWN_REPLACEMENTS == true) {
						householdNetwork.add(new Person(this.space, getRandomAge()));
						context.add(householdNetwork.get(householdNetwork.size() - 1));
						//System.out.println("replacement");
					}
				} else {
					householdNetwork.get(i).migFlag = false;
				}
			}
					
		}
		
		//bring back migrated members over 39
		for (int i = migratedMembers.size() - 1; i >= 0; i--) {
			if (migratedMembers.get(i).age > 39) {
				Context<Object> context = ContextUtils.getContext(this);
				householdNetwork.add(migratedMembers.get(i));
				context.add(migratedMembers.get(i));
				migratedMembers.remove(i);
			}
		}
			
		if ((migrationNetwork + numMigrants) <= PakAbmBuilder.MAX_MIGRATION_NETWORK) {
			migrationNetwork = migrationNetwork + numMigrants;
		}

		//assign new migration network size to all the remaining agents in the household.
		if ((migrationNetwork+numMigrants) <= PakAbmBuilder.MAX_MIGRATION_NETWORK) {	
			for (int i = 0; i < householdNetwork.size(); i++) {
			
				householdNetwork.get(i).migNetworkSize = migrationNetwork;
				
				//System.out.print("i have this many migrant membets: ");
				//System.out.println(this.householdNetwork.get(i).migNetworkSize);
				
			}
		}
			
		
		//increase age of all agents in household and migrated
		for (int i = 0; i <householdNetwork.size(); i++) {
			householdNetwork.get(i).age++;
		}
		for (int i = 0; i < migratedMembers.size(); i++) {
			migratedMembers.get(i).age++;
		}
		//kill those that are too old
		for (int i = householdNetwork.size() - 1; i >= 0; i--) {
			if (householdNetwork.get(i).age > PakAbmBuilder.TERMINAL_AGE) {
				if(householdNetwork.get(i).migratedFlag = true) {
					numMigrants--;
				}
				Context<Object> context = ContextUtils.getContext(this);
				context.remove(householdNetwork.get(i));
				householdNetwork.remove(i);
			}
		}
		
		//System.out.print(count);
		
		//System.out.println(migrationNetwork);
	}
	
	@ScheduledMethod(start=2, interval=1, priority=27)
	public void returnAgents() {
		//System.out.println("running return agens");
		//for all migrated agents, if one has the return flag up, bring back into household network and return to context
		for (int i = migratedMembers.size() -1; i >= 0; i--) {
			//once a member returns, turn on his "migrated" flag.
			migratedMembers.get(i).migratedFlag = true;
			
			if (migratedMembers.get(i).returnFlag == true) {
				Context<Object> context = ContextUtils.getContext(this);
				migratedMembers.get(i).returnFlag = false;
				householdNetwork.add(migratedMembers.get(i));
				context.add(householdNetwork.get(householdNetwork.size() - 1));
				//System.out.println("an agent return fro migraton**********************");
			}
		}
	}
	
	public int getRandomAge() {
		Random ageSeed = new Random();
		Random ageSetter = new Random();
		int ageGen = ageSeed.nextInt(100);
		int ageToSet = 0;
		if (ageGen >= 0 && ageGen < 16) {
			ageToSet = ageSetter.nextInt(5);
		} else if (ageGen >= 16 && ageGen < 33) {
			ageToSet = ageSetter.nextInt(5) + 5;
		} else if (ageGen >= 33 && ageGen < 46) {
			ageToSet = ageSetter.nextInt(5) + 10;
		} else if (ageGen >= 46 && ageGen < 56) {
			ageToSet = ageSetter.nextInt(5) + 15;
		} else if (ageGen >= 56 && ageGen < 65) {
			ageToSet = ageSetter.nextInt(5) + 20;
		} else if (ageGen >= 65 && ageGen < 72) {
			ageToSet = ageSetter.nextInt(5) + 25;
		} else if (ageGen >= 72 && ageGen < 78) {
			ageToSet = ageSetter.nextInt(5) + 30;
		} else if (ageGen >= 78 && ageGen < 83) {
			ageToSet = ageSetter.nextInt(5) + 35;
		} else if (ageGen >= 83 && ageGen < 87) {
			ageToSet = ageSetter.nextInt(5) + 40;
		} else if (ageGen >= 87 && ageGen < 90) {
			ageToSet = ageSetter.nextInt(5) + 45;
		} else if (ageGen >= 90 && ageGen < 93) {
			ageToSet = ageSetter.nextInt(5) + 50;
		} else if (ageGen >= 93 && ageGen < 95) {
			ageToSet = ageSetter.nextInt(5) + 55;
		} else if (ageGen >= 95 && ageGen < 97) {
			ageToSet = ageSetter.nextInt(5) + 60;
		} else if (ageGen >= 97 && ageGen < 98) {
			ageToSet = ageSetter.nextInt(5) + 65;
		} else if (ageGen >= 98 && ageGen < 99) {
			ageToSet = ageSetter.nextInt(5) + 70;
		} else if (ageGen >= 99 && ageGen < 100){
			ageToSet = ageSetter.nextInt(5) + 75;
		}
		
		return ageToSet;
	}

}


