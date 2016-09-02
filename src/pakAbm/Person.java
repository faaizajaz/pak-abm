package pakAbm;

import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.util.ContextUtils;

public class Person {
	
	//migration coefficients
	public double INTERCEPT = -3.68055;
	public double MIG_TEMP = 0.2; 
	public double MIG_NET = 0.56284;
	public double AGE_COEFF = -0.082198;
	
	int count = 0;
	
	//
	
	private ContinuousSpace<Object> space;
	private boolean  migrationDecision;
	private float migrationOpportunity;
	private float migrationGender;
	
	private double migProb;
	
	public static double migrationTemp;
	
	private int famIndex;
	private boolean isMale;
	public int age;	
	public boolean migFlag = false;	
	public int migNetworkSize;
	
	public boolean returnFlag = false;
	public boolean migratedFlag = false;
	//public boolean birthFlag = true;
	
	
	
	//Constructor
	public Person(ContinuousSpace<Object> space, int age) {
		
		//Allocate sex
		Random r = new Random();
		int sexAllocationOdds = r.nextInt(100) + 1;
		
		if (sexAllocationOdds < 57) {
			this.isMale = true;
		} else {
			this.isMale = false;
		}
		
		this.age = age;
		
		//Assign person its family index
		//this.famIndex = famIndex;
		
		
		migrationTemp = PakAbmBuilder.TEMP;		
		
	}
	
	@ScheduledMethod(start=1, interval=1, priority=28)
	public void CalculateMigrationProbability() {
		double tempMigProb = 0.0;
		if (PakAbmBuilder.SOCIAL_NETWORKS_ON == true) {
			//calculate: migration_prob ~ Mig_Net + Mig_Temp
			tempMigProb = 1/(1 + Math.exp(-(INTERCEPT + (this.MIG_NET * this.migNetworkSize) + (this.MIG_TEMP * migrationTemp) /*+ (this.AGE_COEFF * age)*/)));
		} else {
			tempMigProb = 1/(1 + Math.exp(-(INTERCEPT + (this.MIG_NET * 0) + (this.MIG_TEMP * migrationTemp) /*+ (this.AGE_COEFF * age)*/)));
		}
		//apply gendered probabilities of migration
		if (this.isMale == true) {
			this.migProb = 0.80 * tempMigProb;
		} else {
			this.migProb = 0.20 * tempMigProb;
		}
		
		Random mig = new Random();
		int migRandom = mig.nextInt(10000) + 1;
		//System.out.println(this.age);
		if (this.age < 40 && this.age > 14) { //if bw 15 and 29
			if (migRandom < ((this.migProb * 10000)/25) * (39-age)) {  //and probability works out
				//migrate
				Migrate();
				//System.out.println("migrant");
			} else {
				
			}
		}
		
		
	}
	
	public void Migrate(){
		//raise migration flag if migrated, then update agents from Household (uses migration flag to update agent mignetwork)
		this.migFlag = true;
		//this.migratedFlag = true;
		
		Random migReturn = new Random();
		int returnRandom = migReturn.nextInt(100) + 1;
		if (returnRandom < PakAbmBuilder.RETURN_ODDS) {
			returnFlag = true;
		} else {
			returnFlag = false;
		}
		
		PakAbmBuilder.MIGRATION_COUNT += 1;
		
		//System.out.print("********how many migrated/:");
		//System.out.println(PakAbmBuilder.MIGRATION_COUNT);
	}
	
	
	//pick one person to update the context temp
	@ScheduledMethod(start=2, interval=1, pick=1, priority=29)
	public void UpdateContextTemp() {
		//here I also want to update PakAbmBuilder.TEMP so that next time a person is constructed he has correct temp.
		PakAbmBuilder.TEMP = PakAbmBuilder.TEMP + PakAbmBuilder.DTEMP;
		migrationTemp = PakAbmBuilder.TEMP;
		
		//System.out.print("One guy update weather to ");
		//System.out.println(migrationTemp);
		//System.out.print(PakAbmBuilder.count);
		System.out.println(PakAbmBuilder.MIGRATION_COUNT);
		PakAbmBuilder.count++;
		PakAbmBuilder.MIGRATION_COUNT = 0;
	}
	
	
	/*@ScheduledMethod(start=1, interval=1, priority = 28)
	public void test2() {

		System.out.println(migrationTemp);
	}*/
	
	
	
	
	
	
	
	
	

}
