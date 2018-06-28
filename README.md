# Concurrency Control for Airline Database

There are multiple transaction threads. Transaction types include:
1) Reserve(F, i): reserve a seat for passenger with id i on flight F, where i > 0.
2) Cancel(F, i): cancel reservation for passenger with id i from flight F.
3) My_Flights(id): returns the set of flights on which passenger i has a reservation.
4) Total_Reservations(): returns the sum total of all reservations on all flights.
5) Transfer(F1,F2,i): transfer passenger i from flight F1 to F2. This transaction should have no impact if the passenger is not found in F1 or there is no room in F2

The Concurrenct Control Manager must Lock/Unlock objects and allows transaction to proceed.
1) In the first implementation, only serial order of transaction is permitted.
2) Second implementation uses 2PL protocol. Lock ordering is performed to ensure there are no deadlocks.

Refer to Homework2.pdf for more details 
