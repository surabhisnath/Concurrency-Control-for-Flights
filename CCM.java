import java.util.*;
import java.util.concurrent.locks.ReentrantLock;


class Passenger
{
	int id;
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
	int F;
	int num_res;
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
	static ReentrantLock lock =new ReentrantLock();
	int num_flights;
	int num_passengers;
	
	
	void Reserve(int F, int id)
	{
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(db.hmap_flight.get(F).hmap_passengers.get(id)==null)
		{
				 Flight flight=db.hmap_flight.get(F);
				 Passenger passenger=db.hmap_passenger.get(id);
				 
				 flight.num_res++;
				 
				 if(flight.num_res<=flight.capacity)
				 {
					 flight.hmap_passengers.put(id, passenger);
					 passenger.hmap_flights.put(F, flight);
					 System.out.println("Successfully reserved");
				 }
				 
				 else
				 {
					 System.out.println("Flight capacity reached");
				 }
				 
		}
		
		else
		{
			System.out.println("This passenger has already booked this flight");
		}
		
	}
	
	void Cancel(int F, int id)
	{
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		Flight flight=db.hmap_flight.get(F);
		Passenger passenger=db.hmap_passenger.get(id);
		if(flight.hmap_passengers.get(id)!=null)
		{
			flight.hmap_passengers.remove(id);
			passenger.hmap_flights.remove(F);
			System.out.println("Successfully cancelled");
			flight.num_res--;
		}
		
		else
		{
			System.out.println("The passenger has not booked this flight");
		}
		
	}
	
	ArrayList<Flight> My_flight(int id)
	{ 
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ArrayList<Flight> arr=new ArrayList<>();
		for(int i=0;i<db.flights.size();i++)
		{
			Flight flight =db.flights.get(i);
			if(flight.hmap_passengers.get(id)!=null)
			{
				arr.add(flight);
			}
		}
		
		return arr;
		
	}
	
	int Total_Reservation()
	{
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int sum=0;
		
		for(int u=0; u<db.flights.size(); u++)
		{
			sum+=db.flights.get(u).num_res;
		}
		
		return sum;
	}
	
	void Transfer(int F1, int F2, int i)
	{
		try {
			Thread.sleep(250);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Flight flight=db.hmap_flight.get(F1);
		Passenger passenger=db.hmap_passenger.get(i);
		if(flight.hmap_passengers.get(i)!=null)
		{
			flight.hmap_passengers.remove(i);
			passenger.hmap_flights.remove(F1);
			//System.out.println("Successfully cancelled");
			
			if(db.hmap_flight.get(F2).hmap_passengers.get(i)==null)
			{
					 Flight flight1=db.hmap_flight.get(F2);
					 Passenger passenger1=db.hmap_passenger.get(i);
					 
					 flight1.hmap_passengers.put(i,passenger1);
					 passenger1.hmap_flights.put(F2, flight1);
					 
					 flight1.num_res++;
			}
			
			System.out.println("Successfully transferred");
		}
		
		else
		{
			System.out.println("The passenger has not booked flight F1, transfer failed");
		}
		
	}

	@Override
	public void run() 
	{	
		
		while(!lock.tryLock())
		{
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		int cnt=0;
		
		do
		{	
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Random generator = new Random();
			int trans_type;
						
			double prob = generator.nextDouble() + generator.nextDouble(); 
			
			if(prob<1.2)
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
				System.out.println("RESERVE");
				int F = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
				
				Reserve(F,id);
			}
			
			else if(trans_type == 2)	//cancel
			{
				System.out.println("CANCEL");
				int F = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
				
				Cancel(F,id);
			}
			
			else if(trans_type == 3)	//my flights
			{
				System.out.println("MY_FLIGHTS");
				int x=generator.nextInt(num_passengers)+1;
				ArrayList<Flight> t = My_flight(x);
				
				if(t.size()==0)
				{
					System.out.print("Flight ids for reserved flights for passenger "+x+": none");
				}
				
				else
				{
					for(int u=0; u<t.size(); u++)
					{
						System.out.print("Flight ids for reserved flights for passenger "+x+": ");
						System.out.print(t.get(u).F+" ");
					}
				}
				
				System.out.println();
			}
			
			else if(trans_type == 4)	//total reservations
			{
				System.out.print("TOTAL RESERVATIONS: ");
				System.out.println(Total_Reservation());
			}
			
			else if(trans_type == 5)	//transfer
			{
				System.out.println("TRANSFER");
				int F1 = generator.nextInt(num_flights)+1;
				int F2 = generator.nextInt(num_flights)+1;
				int id = generator.nextInt(num_passengers)+1;
			
				Transfer(F1, F2, id);
			}
			
			cnt++;
			
		}while(cnt<5);
		
		lock.unlock();
		
		 
	}
}

class Database 
{
	volatile ArrayList<Flight> flights=new ArrayList<Flight>();
	volatile ArrayList<Passenger> passengers = new ArrayList<Passenger>();
	volatile ArrayList<Transaction> transaction = new ArrayList<Transaction>();
	volatile HashMap<Integer, Flight> hmap_flight = new HashMap<Integer, Flight>();
	volatile HashMap<Integer, Passenger> hmap_passenger = new HashMap<Integer, Passenger>();
}

public class CCM
{
	public static void main(String[] args) 
	{
		Database obj=new Database();
			
		//int num_passengers = generator.nextInt(6)+5; //Generate a random number of passengers (5 to 10)
		//int num_flights = generator.nextInt(3)+3; //Generate a random number of passengers (3 to 5)
		
		int num_passengers=10;
		int num_flights=4;
		
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
			

		int num_trans =1;
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
			pool[y].run();
		}
		
		long endTime   =System.currentTimeMillis();
		double totalTime =( endTime - startTime)/1000;
		System.out.println("Time taken: "+totalTime);
		System.out.println("Throughput: "+num_trans*5/totalTime);
	}
}