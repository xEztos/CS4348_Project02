import java.util.concurrent.Semaphonre;

public class Project2{
	private static Semaphore enteredPatient;
	private static Semaphore readyReceptionest;
	private static Semaphore waitingPatient[];
	private static Semaphore notifiedNurse[];
	private static Semaphore availableNurse[];
	private static Semaphore officeDirection[];
	private static Semaphore officePatient[];
	private static Semaphore notifiedDoctor[];
	private static Semaphore availableDoctor[];
	private static Semaphore symptomListen[];
	private static Semaphore doctorAdvice[];
	private static Semaphore patientLeaves[];

	public static void main(String[] args){
		// int numReceptionest = 1;	// number of receptionest (1)
		int numDoctors = 3;			// maximum number of doctors is 3
		int numPatients = 30;		// maximum number of patients is 30
		
		for(int argIndex = 0; argIndex < args.length; argIndex++){
			char[] argFlag = args[argIndex].toCharArray();

			try{
				switch (argFlag[0]){
					case '-':
						switch(argFlag[1]){
							case 'd':	// number of doctors
								numDoctors = Integer.parseInt(args[++argIndex]);
								break;
							case 'p':	// number of patients
								numPatients = Integer.parseInt(args[++argIndex]);
								break;
							default:
								break;
						}
						break;
					default:	// invalid parameter
						break;
				}
			} catch(ArrayIndexOutOfBoundsException e){
				//array index out of bounds
			}
		}


		// Initialize Threads
		Thread receptionest = new Thread(new Receptionest());

		Thread doctor[] = new Thread[numDoctors];
		Thread nurse[] = new Thread[numDoctors];
		for(int i = 0; i < numDoctors; i++){
			doctor[i] = new Thread(new Doctor(i));
			nurse[i] = new Thread(new Nurse(i));
		}

		Thread patient[] = new Thread[numPatients];
		for(int i = 0; i < numPatients; i++){
			patient[i] = new Thread(new Patient(i));
		}

		// Initialize Semaphores
		enteredPatient = new Semaphore(0, true);
		readyReceptionest = new Semaphore(0, true);
		for(int i = 0; i < numDoctors; i++){
			waitingPatient = new Semaphore(0, true);
			notifiedNurse = new Semaphore(0, true);
			availableNurse = new Semaphore(0, true);
			officeDirection = new Semaphore(0, true);
			officePatient = new Semaphore(0, true);
			notifiedDoctor = new Semaphore(0, true);
			availableDoctor = new Semaphore(0, true);
			symptomListen = new Semaphore(0, true);
			doctorAdvice = new Semaphore(0, true);
			patientLeaves = new Semaphore(0, true);
		}
	}

	public static class Receptionest implements Runnable{
		// private int receptionestID;
		// public Receptionest(int id){ this.receptionestID = id };
		public void run(){
			try{
				readyReceptionest.release();
			} catch(InteruptedException e){System.out.println("InterruptedException in Receptionest")}
		}
	}

	public class Doctor implements Runnable{
		private int doctorID;
		public Doctor(int id){ this.doctorID = id };
		public void run(){
			try{

			} catch(InteruptedException e){System.out.println("InterruptedException in Doctor " + doctorID);}

		}
	}

	public class Patient implements Runnable{
		private int patientID;
		public Patient(int id){ this.patientID = id };
		public void run(){
			try{

			} catch(InteruptedException e){System.out.println("InterruptedException in Patient " + patientID);}

		}
	}

	public class Nurse implements Runnable{
		private int nurseID;
		public Nurse(int id){ this.patientID = id };
		public void run(){
			try{

			} catch(InteruptedException e){System.out.println("InterruptedException in Nurse " + nurseID);}

		}
	}
}