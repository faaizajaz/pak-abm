package pakAbm;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.RandomCartesianAdder;

public class PakAbmBuilder implements ContextBuilder<Object> {
	
	private double initTemp = 19.75;
	public static double TEMP;
	public static double DTEMP = 0.03;
	public static double RETURN_ODDS = 70;
	public static int BIRTH_RATE = 4;
	public static int DEATH_RATE = 1;
	public static int TERMINAL_AGE = 70;
	
	public static int IN_MIGRATION = 0;
	public static int MAX_MIGRATION_NETWORK = 8;
	
	public static boolean SPAWN_REPLACEMENTS = true;
	public static boolean SOCIAL_NETWORKS_ON = true;
	
	public static int MIGRATION_COUNT = 0;


	
	
	public static int ADULT_AGE = 20;
	
	public static int count = 0;
	
	boolean sweepmignet = false;
	
	
	
	
	
	@SuppressWarnings("rawtypes")
	@Override
	public Context build(Context<Object> context) {
		
		context.setId("PakAbm");
		
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace("space",
				context, 
				new RandomCartesianAdder<Object>(), 
				new repast.simphony.space.continuous.WrapAroundBorders(),
				50, 50);
		
		
		int numHouseholds = 1000;
		int averageHousehold = 7;
		TEMP = initTemp;
		//System.out.println("set to initial temp");
		
		Random householdSizeGenerator = new Random();
		Random migNetworkSizeGenerator = new Random();
		

		
		for (int i = 0; i < numHouseholds; i++) {
			//create households with size such that average equals the average household size	
			int householdSize =  (int) Math.round(householdSizeGenerator.nextGaussian()* 1 + averageHousehold);
			
			//populate initial migration network size based on odds calculated from IPRF data
			int migNetworkSize = 0;
			
			int migrationNetworkSeed = migNetworkSizeGenerator.nextInt(11220);
			
			if (sweepmignet == false) {
 				if (migrationNetworkSeed < 56) {
					migNetworkSize = 4;
				} else if (migrationNetworkSeed >= 56 && migrationNetworkSeed < 216) {
					migNetworkSize = 3;
				} else if (migrationNetworkSeed >= 216 && migrationNetworkSeed < 664)  {
					migNetworkSize = 2;
				} else if (migrationNetworkSeed >= 664 && migrationNetworkSeed < 5670) {
					migNetworkSize = 1;
				} else {
					migNetworkSize = 0;
				}
			
			} else {
				migNetworkSize = 0;
			}
			Household newHousehold = new Household(space, householdSize, migNetworkSize);
			//System.out.println(householdSize);
			//System.out.println(migNetworkSize);
			context.add(newHousehold);
			
			for (Object obj : newHousehold.householdNetwork) {
				context.add(obj);
			}
			
		}
		

		
		return context;
	}
	
	public int CountMigrants() {
		return PakAbmBuilder.MIGRATION_COUNT;
	}
	

}
