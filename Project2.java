import java.util.concurrent.Semaphore;

/**
 * Project 2 for CS 4348 Operating Systems. This is a project that simulates the journey of a patient through a clinic.
 *
 *	The clinic to be simulated has doctors, each of which has their own nurse. Each doctor has an office of his or her own in which to visit patients. Patients 
 *	will enter the clinic to see a doctor, which should be randomly assigned. Initially, a patient enters the waiting room and waits to register with the receptionist. 
 *	Once registered, the patient sits in the waiting room until the nurse calls. The receptionist lets the nurse know a patient is waiting. The nurse directs the 
 *	patient to the doctor’s office and tells the doctor that a patient is waiting. The doctor visits the patient and listens to the patient’s symptoms. The doctor 
 *	advises the patient on the action to take. The patient then leaves.
 */
public class Project2{
	private static Semaphore enteredPatient;
	private static Semaphore readyReceptionest;
	private static Semaphore givePatientID;
	private static Semaphore declaredOfficeNumber;
	private static Semaphore receptionLeave;
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

	// Shared resources
	private static int registerPatientID;
	private static int officeNumber;
	private static int numDoctors;
	private static int numPatients;
	private static int waitingToOffice[] = new int[3];
	private static int inOffice[] = new int[3];


	private static int SLEEP_DELAY = 1000;


	/**
	 *	Main running method of the Project.
	 *	@param args the arguments that modify the number of doctors, the number of patients, and the delay
	 */
	public static void main(String[] args){
		// int numReceptionest = 1;	// number of receptionest (1)
		numDoctors = 3;			// maximum number of doctors is 3
		numPatients = 3;		// maximum number of patients is 30
		
		for(int argIndex = 0; argIndex < args.length; argIndex++){
			char[] argFlag = args[argIndex].toCharArray();

			try{
				switch (argFlag[0]){
					case '-':
						switch(argFlag[1]){
							case 'd':	// number of doctors
							case 'D':
								numDoctors = Integer.parseInt(args[++argIndex]);
								break;
							case 'p':	// number of patients
							case 'P':
								numPatients = Integer.parseInt(args[++argIndex]);
								break;
							case 's':
							case 'S':
								SLEEP_DELAY = Integer.parseInt(args[++argIndex]);
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

		System.out.printf("Run with %d patients, %d nurses, %d doctors %n%n", numPatients, numDoctors, numDoctors);

		// Initialize Semaphores
		enteredPatient = new Semaphore(0, true);
		readyReceptionest = new Semaphore(0, true);
		givePatientID = new Semaphore(0,true);
		declaredOfficeNumber = new Semaphore(0,true);
		receptionLeave = new Semaphore(0,true);
		waitingPatient = new Semaphore[numDoctors];
		notifiedNurse = new Semaphore[numDoctors];
		availableNurse = new Semaphore[numDoctors];
		officeDirection = new Semaphore[numDoctors];
		officePatient = new Semaphore[numDoctors];
		notifiedDoctor = new Semaphore[numDoctors];
		availableDoctor = new Semaphore[numDoctors];
		symptomListen = new Semaphore[numDoctors];
		doctorAdvice = new Semaphore[numDoctors];
		patientLeaves = new Semaphore[numDoctors];
		for(int i = 0; i < numDoctors; i++){
			waitingPatient[i] = new Semaphore(0, true);
			notifiedNurse[i] = new Semaphore(0, true);
			availableNurse[i] = new Semaphore(0, true);
			officeDirection[i] = new Semaphore(0, true);
			officePatient[i] = new Semaphore(0, true);
			notifiedDoctor[i] = new Semaphore(0, true);
			availableDoctor[i] = new Semaphore(0, true);
			symptomListen[i] = new Semaphore(0, true);
			doctorAdvice[i] = new Semaphore(0, true);
			patientLeaves[i] = new Semaphore(1, true);
		}


		// Initialize and Start Threads
		Thread receptionest = new Thread(new Receptionest());
		receptionest.start();

		Thread doctor[] = new Thread[numDoctors];
		Thread nurse[] = new Thread[numDoctors];
		for(int i = 0; i < numDoctors; i++){
			doctor[i] = new Thread(new Doctor(i));
			nurse[i] = new Thread(new Nurse(i));

			doctor[i].start();
			nurse[i].start();
		}

		Thread patient[] = new Thread[numPatients];
		for(int i = 0; i < numPatients; i++){
			patient[i] = new Thread(new Patient(i));
			patient[i].start();
		}


		// Waits for all the patients to finished ex2ecuting
		for(int i = 0; i < numPatients; i++){
			try{
				patient[i].join();
			} catch (InterruptedException e) {}
		}


		// receptionest.interrupt();
		// for(int i = 0; i < numDoctors; i++){
		// 	doctor[i].interrupt();
		// 	nurse[i].interrupt();
		// }

		System.exit(0);


	}

	/**
	 *	Simulates a receptionest at a hospital or a clinic that registers a patient and assigns them to a random doctor
	 */
	static class Receptionest implements Runnable{
		boolean running;
		// private int receptionestID;
		// public Receptionest(int id){ this.receptionestID = id };
		public void run(){
			running = true;
			try{
				while(running){
					//	Receptionest waits for a entered patient
					readyReceptionest.release();
						// System.out.println("Receptionest is ready to service a patient.")
					enteredPatient.acquire();

					//	Receptionest waits for the patient to give their patient ID, and gives the patient a random doctor's office
					givePatientID.acquire();
					System.out.printf("Receptionest registers patient %d.%n", registerPatientID);
					Thread.sleep(SLEEP_DELAY);
					officeNumber = new java.util.Random().nextInt(numDoctors);
					declaredOfficeNumber.release();

					// Notify the nurse of the patient
					notifiedNurse[officeNumber].release();


					//	Receptionest waits for patient to leave reception window
					receptionLeave.acquire();
				}
			} catch(InterruptedException e){System.out.println("InterruptedException in Receptionest");}
		}

		// public void exit(int exit_code){
		// 	running=false;
		// 	System.out.printf("Receptionest is exiting");
		// }
	}

	/**
	 *	Simulates a doctor that first waits for the nurse to inform him of a waiting patient, listens to that patient's symptoms, and advises on further actions.
	 */
	static class Doctor implements Runnable{
		private int doctorID;

		/**
		 *	The constructor for the doctor that works in the clinic. The doctor's id is the same as the nurse's id and the office id
		 *	@param id the id number of the doctor that must be the same as the nurse he is assigned
		 */
		public Doctor(int id){ this.doctorID = id; }
		public void run(){
			try{
				while(true){
					availableDoctor[doctorID].release();

					// Waits to be notified and for the patient to make his way to the room
					notifiedDoctor[doctorID].acquire();
					officePatient[doctorID].acquire();

					// Doctor is available to listen to symptoms
					symptomListen[doctorID].acquire();
					System.out.printf("Doctor %d listens to symptoms from patient %d. %n", doctorID, inOffice[doctorID]);
					Thread.sleep(SLEEP_DELAY);

					//	Doctor gives advices from symptoms
					doctorAdvice[doctorID].release();

				}

			} catch(InterruptedException e){System.out.println("InterruptedException in Doctor " + doctorID);}

		}
	}

	/**
	 *	Simulates the patients that goes through a doctor's office:
	 *		- Registers with the receptionest
	 *		- Waits in the waiting room for the nurse
	 *		- Waits in the doctor's office for the doctor
	 *		- "tells" the doctor of the patient's symptoms
	 *		- "recieves" the advice from the doctor
	 *		- leaves the doctor's office
	 */
	static class Patient implements Runnable{
		private int patientID;
		private int assignedOffice;

		/**
		 *	Constructor for the patient that treks through the clinic
		 *	@param id the unique ID number of this patient
		 */
		public Patient(int id){ this.patientID = id; }

		public void run(){
			try{

				//	Patient enters waiting room and waits for an open receptionest
				enteredPatient.release();
				System.out.printf("Patient %d enters the waiting room, waits for receptionest. %n", patientID);
				Thread.sleep(SLEEP_DELAY);
				readyReceptionest.acquire();

				//	Patient gives their ID to the receptionest
				registerPatientID = patientID;
				givePatientID.release();

				//	Patient waits and take the assigned office, and sits and waits for nurse
				declaredOfficeNumber.acquire();
				assignedOffice = officeNumber;
				System.out.printf("Patient %d leaves receptionest and sits in the waiting room. %n", patientID);
				Thread.sleep(SLEEP_DELAY);
				receptionLeave.release();

				// Patient waits for nurse to direct to instructed office
				waitingPatient[assignedOffice].release();
				availableNurse[assignedOffice].acquire();
				waitingToOffice[assignedOffice] = patientID;
				officeDirection[assignedOffice].acquire();

				//Patient sits in instructed office waiting for the doctor to come and listen to symptoms
				System.out.printf("Patient %d enter doctor %d's office. %n", patientID, assignedOffice);
				Thread.sleep(SLEEP_DELAY);
				inOffice[assignedOffice] = patientID;
				officePatient[assignedOffice].release();
				availableDoctor[assignedOffice].acquire();

				//	Patient gives symptoms and waits for doctor to give advice
				symptomListen[assignedOffice].release();
				doctorAdvice[assignedOffice].acquire();
				System.out.printf("Patient %d recieves advice from doctor %d's office. %n", patientID, assignedOffice);
				Thread.sleep(SLEEP_DELAY);

				//	Patient leaves the office
				System.out.printf("Patient %d leaves. %n", patientID);
				patientLeaves[assignedOffice].release();

			} catch(InterruptedException e){System.out.println("InterruptedException in Patient " + patientID);}

		}
	}


	/**
	 *	Simulates a nurse in a doctor's office, that takes a patient from the waiting room to the assigned doctor's office and informs the doctor
	 */
	static class Nurse implements Runnable{
		private int nurseID;

		/**
		 *	Constructor for the simulated nurse.
		 *	@param id the office this nurse is associated with (must be the same as the assigned doctor id)
		 */
		public Nurse(int id){ this.nurseID = id; }

		public void run(){
			try{
				while(true){
					availableNurse[nurseID].release();

					//	wait for patient to finish registering, and for receptionest to notify the nurse
					waitingPatient[nurseID].acquire();
					notifiedNurse[nurseID].acquire();

					//	Nurse waits for the office to be empty
					patientLeaves[nurseID].acquire();

					//	directs patient to the doctor's office and notify the doctor
					System.out.printf("Nurse %d takes patient %d to doctor's office. %n", nurseID, waitingToOffice[nurseID]);
					Thread.sleep(SLEEP_DELAY);
					officeDirection[nurseID].release();
					notifiedDoctor[nurseID].release();
				}
			} catch(InterruptedException e){System.out.println("InterruptedException in Nurse " + nurseID);}

		}
	}
}