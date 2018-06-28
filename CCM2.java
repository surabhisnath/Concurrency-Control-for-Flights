import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.*;


class Passenger
{
	int id;
	ReentrantLock pass_lock =new ReentrantLock();
	volatile int lock_mode=-1;
	volatile int read_count=0;
	HashMap<Integer,Flight> hmap_flights = new HashMap<Integer,Flight>();
	
	
	public Passenger(int i)
	{
		id=i;
	}
	
	public void setflight(Flight flight)
	{
		hmap_flights.put(flight.F, flight);
	}
}

class Flight
{
	ReentrantLock flight_lock =new ReentrantLock();
	int F;
	volatile int num_res;
	volatile int lock_mode=-1;
	volatile int read_count=0;
	//ArrayList<Passenger> passengers;
	HashMap<Integer,Passenger> hmap_passengers = new HashMap<Integer,Passenger>();
	int capacity;
	
	public Flight(int f, int c)
	{
		F=f;
		capacity = c;
		num_res=0;
	}
}

class Transaction implements Runnable
{
	Database db;
	String type;
	//static ReentrantLock lock =new ReentrantLock();
	int num_flights;
	int num_passengers;
	int gl=0;
	
	
	void Reserve(int F, int id, int cnt) throws InterruptedException
	{	
		Thread.sleep(250);
		if(db.hmap_flight.get(F).hmap_passengers.get(id)==null)
		{

			 Flight flight=db.hmap_flight.get(F);
			 Passenger passenger=db.hmap_passenger.get(id);
			 
				//while(!flight.flight_lock.tryLock() || flight.lock_mode==0)		//Checks if there is an exclusive lock or there is an shared lock
			 	while(flight.lock_mode==1 || flight.lock_mode==0)
			 	{
					//System.out.println("1");
					 Thread.sleep(1000);
				 }
				 
				 //while(!passenger.pass_lock.tryLock() || passenger.lock_mode==0)
			 	 while(passenger.lock_mode==1 || passenger.lock_mode==0)
				 {
					 //System.out.println("2");
					 Thread.sleep(1000);
				 }
				 
				 flight.lock_mode=1;
				 passenger.lock_mode=1;
				 
				 flight.num_res++;
				 
				 if(flight.num_res<=flight.capacity)
				 {
					 flight.hmap_passengers.put(id, passenger);
					 passenger.hmap_flights.put(F, flight);
					 System.out.println("RESERVE\nSuccessfully reserved"); 
				 }
				 
				 else
				 {
					 System.out.println("RESERVE\nFlight capacity reached");
				 }
				 
				 
				 
				 flight.lock_mode=-1;
				 passenger.lock_mode=-1;
				 
				 //flight.flight_lock.unlock();
				 //passenger.pass_lock.unlock();
		}
		
		else
		{
			System.out.println("RESERVE\nThis passenger has already booked this flight");
		}
		
	}
	
	void Cancel(int F, int id, int cnt) throws InterruptedException
	{
		
		Thread.sleep(250);
		Flight flight=db.hmap_flight.get(F);
		Passenger passenger=db.hmap_passenger.get(id);
		
		if(flight.hmap_passengers.get(id)!=null)
		{	
			//while( !flight.flight_lock.tryLock() || flight.lock_mode==0)
			while(flight.lock_mode==1 || flight.lock_mode==0)
			{
				//System.out.println("3");
				 Thread.sleep(1000);
			}
			 
			//while(!passenger.pass_lock.tryLock() || passenger.lock_mode==0)
			while(passenger.lock_mode==1 || passenger.lock_mode==0)
			{
				//System.out.println("4");
				 Thread.sleep(1000);
			}
			
			flight.lock_mode=1;
			passenger.lock_mode=1;
			
			flight.hmap_passengers.remove(id);
			passenger.hmap_flights.remove(F);
			flight.num_res--;
			System.out.println("CANCEL\nSuccessfully cancelled");
			
			
			flight.lock_mode=-1;
			passenger.lock_mode=-1;
			 
			//flight.flight_lock.unlock();
			//passenger.pass_lock.unlock();
			
		}
		
		else
		{
			System.out.println("CANCEL\nThe passenger has not booked this flight");
		}
	}
	
	ArrayList<Flight> My_flight(int id, int cnt) throws InterruptedException
	{ 
		Thread.sleep(250); 
		ArrayList<Flight> arr=new ArrayList<>();
		
		
		for(int i=0; i<db.flights.size(); i++)
		{
			Flight flight =db.flights.get(i);
			
			//while(flight.flight_lock.isLocked())
			while(flight.lock_mode==1)
			{
				//System.out.println("5");
				 Thread.sleep(1000);
			}
			
			flight.flight_lock.lock();
			flight.read_count++;
			flight.flight_lock.unlock();
			
			flight.lock_mode=0;
			
			
			if(flight.hmap_passengers.get(id)!=null)
			{
				arr.add(flight);
			}
		}
		
		for(int i=0;i<db.flights.size();i++)
		{
			db.flights.get(i).flight_lock.lock();
			db.flights.get(i).read_count-- ;
			db.flights.get(i).flight_lock.unlock();
			
			//System.out.println("reada"+i+" "+db.flights.get(i).read_count+ "Trans no."+Thread.currentThread()+" "+"cnt"+cnt);
			if(db.flights.get(i).read_count==0)
			{
				//System.out.println("a");
				db.flights.get(i).lock_mode=-1;
			}
		}
		
		return arr;
	}
	
	int Total_Reservation(int cnt) throws InterruptedException
	{
		Thread.sleep(250);
		int sum=0;
		
		for(int u=0; u<db.flights.size(); u++)
		{
			Flight flight =db.flights.get(u);		
			
			//while(flight.flight_lock.isLocked())
			while(flight.lock_mode==1)
			{
				//System.out.println("6");
				 Thread.sleep(1000);
			}
			
			//System.out.println("initial"+u+" "+flight.read_count+Thread.currentThread()+" "+"cnt"+cnt);
			
			flight.flight_lock.lock();
			flight.read_count++;
			flight.flight_lock.unlock();
			
			flight.lock_mode=0;			
			sum+=flight.num_res;
		}
		
		System.out.println("TOTAL RESERVATIONS: "+sum);

		for(int i=0; i<db.flights.size(); i++)
		{
			db.flights.get(i).flight_lock.lock();
			db.flights.get(i).read_count-- ;
			db.flights.get(i).flight_lock.unlock();
			
			//System.out.println("readb"+i+" "+db.flights.get(i).read_count);
			if(db.flights.get(i).read_count==0)
			{
				//System.out.println("b");
				db.flights.get(i).lock_mode=-1;
			}
			
		}
		
		return sum;
	}
	
	void Transfer(int F1, int F2, int i, int cnt) throws InterruptedException
	{
		Thread.sleep(250);
		Flight flight=db.hmap_flight.get(F1);
		Passenger passenger=db.hmap_passenger.get(i);
		
		Flight flight1=db.hmap_flight.get(F2);
		Passenger passenger1=db.hmap_passenger.get(i);
		
		if(flight.hmap_passengers.get(i)!=null)
		{
			if(F1<F2)
			{
				//while(!flight.flight_lock.tryLock() || flight.lock_mode==0)
				while(flight.lock_mode==1 || flight.lock_mode==0)
				{
					//System.out.println("7");
					Thread.sleep(1000);
				}
				
				//while(!flight1.flight_lock.tryLock() || flight1.lock_mode==0)
				while(flight1.lock_mode==1 || flight1.lock_mode==0)
				{
					//System.out.println("8");
					Thread.sleep(1000);
				}
				
				flight.lock_mode=1;
				flight1.lock_mode=1;
			}
			
			else
			{
				//while(!flight1.flight_lock.tryLock() || flight1.lock_mode==0)
				while(flight1.lock_mode==1 || flight1.lock_mode==0)
				{
					//System.out.println("9");
					Thread.sleep(1000);
				}
				
				//while(!flight.flight_lock.tryLock() || flight.lock_mode==0)
				while(flight.lock_mode==1 || flight.lock_mode==0)
				{
					//System.out.println("10");
					Thread.sleep(1000);
				}
				
				flight1.lock_mode=1;
				flight.lock_mode=1;
			}
			
			//while(!passenger.pass_lock.tryLock() || passenger.lock_mode==0)
			while(passenger.lock_mode==1 || passenger.lock_mode==0)
			{
				//System.out.println("11");
				 Thread.sleep(1000);
			}
			
			passenger.lock_mode=1;
			
			flight.hmap_passengers.remove(i);
			passenger.hmap_flights.remove(F1);
			flight.num_res--;
			
			if(db.hmap_flight.get(F2).hmap_passengers.get(i)==null)
			{ 
				 flight1.hmap_passengers.put(i,passenger1);
				 passenger1.hmap_flights.put(F2, flight1);
				 flight1.num_res++;
			}
			
			else
			{
				//Passenger already in F2
			}
			

			System.out.println("TRANSFER\nSuccessfully transferred ");
			
			flight.lock_mode=-1;
			flight1.lock_mode=-1;
			passenger.lock_mode=-1;
			
			//flight.flight_lock.unlock();
			//flight1.flight_lock.unlock();
			//passenger.pass_lock.unlock();

		}
		
		else
		{
			System.out.println("TRANSFER\nThe passenger has not booked flight F1, transfer failed");
		}
		
	}

	@Override
	public void run() 
	{	
		
		 int cnt=0;
	
		do
		{		
			Random generator = new Random();
			int trans_type; 
			
			//double prob =Math.random();
			double prob = generator.nextDouble() + generator.nextDouble(); 
			
			if(prob<1)
			{
				int temp = generator.nextInt(2);
				if(temp==0)
					trans_type=5;
				else
					trans_type=1;
			}
			
			else
			{
				trans_type = generator.nextInt(3)+2;
			}
			
			
			
			if(trans_type == 1)	//reserve
			{
				int F = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
				
				try {
					Reserve(F,id, cnt);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			else if(trans_type == 2)	//cancel
			{
				int F = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
	
				try {
					Cancel(F,id, cnt);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			else if(trans_type == 3)	//my flights
			{
				int x=generator.nextInt(num_passengers)+1;
				ArrayList<Flight> t;
				try {
					t = My_flight(x, cnt);
					String str="";
					
					for(int u=0; u<t.size(); u++)
					{
						str+=t.get(u).F+" ";
					}

					if(str.equals(""))
						str="none";
					System.out.println("MY_FLIGHTS\nFlight ids for reserved flights for passenger "+x+": "+str);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				
			}
			
			else if(trans_type == 4)	//total reservations
			{
				
				try {
					Total_Reservation(cnt);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			else if(trans_type == 5)	//transfer
			{
				int F1 = generator.nextInt(num_flights)+1;
				int F2 = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
			
				try {
					Transfer(F1, F2, id, cnt);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			cnt++;
			
		}while(cnt<5);
		
		
	}
}

class Database 
{
	volatile ArrayList<Flight> flights=new ArrayList<Flight>();
	volatile ArrayList<Passenger> passengers = new ArrayList<Passenger> ();
	volatile ArrayList<Transaction> transaction = new ArrayList<Transaction>();
	volatile HashMap<Integer, Flight> hmap_flight = new HashMap<Integer, Flight>();
	volatile HashMap<Integer, Passenger> hmap_passenger = new HashMap<Integer, Passenger> () ;
}

public class CCM2
{
	public static void main(String[] args) 
	{
		Database obj=new Database();
		
		int num_passengers=10; //Generate a random number of passengers (5 to 10)
		int num_flights =4; //Generate a random number of passengers (3 to 5)
			
		for(int j=0; j<num_flights; j++)
		{
			Random generator = new Random();
			int c = generator.nextInt(3)+3;
			Flight f = new Flight(j+1, c);
			obj.flights.add(f);
			obj.hmap_flight.put(f.F,f);
		}
			
		for(int i=0; i<num_passengers; i++)
		{
			Passenger p = new Passenger(i+1);
			obj.passengers.add(p);
			obj.hmap_passenger.put(p.id, p);
				
			Flight f=obj.flights.get(i%num_flights);				 
			f.hmap_passengers.put(p.id,p);
			p.hmap_flights.put(f.F,f);
			f.num_res++;
		}
			

		int num_trans = 15;
		Transaction arr[]=new Transaction[num_trans];
		for(int i=0;i<num_trans;i++)
		{
			arr[i]=new Transaction();
			arr[i].db=obj;
			arr[i].num_flights = num_flights;
			arr[i].num_passengers = num_passengers;
		}
		
		Thread pool[]=new Thread[num_trans];
			
		for(int i=0;i<num_trans;i++)
		{
			pool[i]=new Thread(arr[i]);
		}
		
		
		long startTime = System.currentTimeMillis();
		
		for(int y=0; y<num_trans; y++)
		{
			pool[y].start();
			
		}
		
		for(int y=0; y<num_trans; y++)
		{
			try 
			{
				pool[y].join();
			} 
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
		}
		
		
		long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime)/1000;
		System.out.println("Time taken: "+totalTime);
		System.out.println("Throughput: "+num_trans*5/totalTime);
		
	}
}