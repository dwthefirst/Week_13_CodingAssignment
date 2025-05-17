package pet.store.service;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreData;
import pet.store.controller.model.PetStoreData.PetStoreCustomer;
import pet.store.controller.model.PetStoreData.PetStoreEmployee;
import pet.store.dao.CustomerDao;
import pet.store.dao.EmployeeDao;
import pet.store.dao.PetStoreDao;
import pet.store.entity.Customer;
import pet.store.entity.Employee;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {

	// instance variable
	@Autowired
	private PetStoreDao petStoreDao;
	
	@Autowired
	private EmployeeDao employeeDao;
	
	@Autowired
	private CustomerDao customerDao;
	
	
	//Methods
	
	//delete pet store by id
	@Transactional(readOnly = false)
	public void deletePetStoreById(Long petStoreId) {
		PetStore petStore = findPetStoreById(petStoreId);
		petStoreDao.delete(petStore);
	}
	
	
	//get PetStore by ID
	@Transactional(readOnly = true)
	public PetStoreData retrievePetStoreById(Long petStoreId) {
		return new PetStoreData(findPetStoreById(petStoreId));

	}
	
	
	//get all pet stores
	@Transactional(readOnly = true)
	public List<PetStoreData> retrieveAllPetStores() {
		List<PetStore> petStores = petStoreDao.findAll();
		
		//Convert the List of PetStore objects to a List of PetStoreData objects. 
		List<PetStoreData> result = new LinkedList<>();
		
		for(PetStore petStore : petStores) {
			PetStoreData psd = new PetStoreData(petStore);
			
			//Remove all customer and employee objects in each PetStoreData object.
			psd.getCustomers().clear();
			psd.getEmployees().clear();
			
			result.add(psd);			
		}
		return result;
	}
	
	
	//saveCustomer
	@Transactional
	public PetStoreCustomer saveCustomer(Long petStoreId, PetStoreCustomer petStoreCustomer) {
		
		PetStore petStore = findPetStoreById(petStoreId);
		Long customerId = petStoreCustomer.getCustomerId();
		Customer customer = findOrCreateCustomer(petStoreId, customerId); //retrieve existing employee or create a new one
		
		
		//copy petStoreCustomer fields to customer
		copyCustomerFields(customer, petStoreCustomer);
		
		//Customer and PetStore has a MANY TO MANY RELATIONSHIP! (so have to add both on their set's of each other)
		customer.getPetStores().add(petStore); //add petstore to the customer's set of petstores
		petStore.getCustomers().add(customer); //adding customer to the petstore's set of customers
		
		Customer dbCustomer = customerDao.save(customer);
		return new PetStoreCustomer(dbCustomer);
		
	}
	
	
	
	private void copyCustomerFields(Customer customer, PetStoreCustomer petStoreCustomer) {
		customer.setCustomerId(petStoreCustomer.getCustomerId());
		customer.setCustomerFirstName(petStoreCustomer.getCustomerFirstName());
		customer.setCustomerLastName(petStoreCustomer.getCustomerLastName());
		customer.setCustomerEmail(petStoreCustomer.getCustomerEmail());
	}



	//find or create customer - uses findCustomerByID() method
	private Customer findOrCreateCustomer(Long petStoreId, Long customerId) {
		// if customer id is null, then return a new customer.
		if(Objects.isNull(customerId)) {
			return new Customer();
		}
		
		//if not null, then call findCustomerById()
		return findCustomerById(petStoreId, customerId);
	}


	
	
	
	private Customer findCustomerById(Long petStoreId, Long customerId) {
		
		Customer customer = customerDao.findById(customerId).orElseThrow(() -> new NoSuchElementException("Customer with ID=" + customerId + " was not found."));
		
		//Customer and PetSTore has many to many relationship
		//Customer has a Set<PetStore> so need to loop through set of PetStore objects looking for the petstore with the given id
			//if not found, throw exception
		boolean found = false;
		
		for(PetStore petStore : customer.getPetStores()) {
			if(petStore.getPetStoreId() == petStoreId) { //checking if the petStoreId from the loop matches the petStoreId passed in as param
				found = true;
				break;
			}
		}
		
		//if none of the petstore (and petStoreId) matches, throw exception
		if(!found) {
			throw new IllegalArgumentException("The customer with ID=" + customerId + " is not a memeber of the pet store with ID=" + petStoreId);
		}
		
		return customer;
	}


	

	//saveEmployee - PetStoreEmployee
	@Transactional(readOnly = false)
	public PetStoreEmployee saveEmployee(Long petStoreId, PetStoreEmployee petStoreEmployee) {
		
		//find the PetStore given the petStoreId parameter passed in
		PetStore petStore = findPetStoreById(petStoreId);
		
		Long employeeId = petStoreEmployee.getEmployeeId();
		
		//retrieve existing employee or create a new one given the PetStoreEmployee passed in - and getting the employeeID from that 
		Employee employee = findOrCreateEmployee(petStoreId, employeeId);
		
		//copy all matching PetStoreEmployee fields to the Employee Object
		copyEmployeeFields(employee, petStoreEmployee);	
		
		employee.setPetStore(petStore); // set the PetStore object in the Employee object.		
		petStore.getEmployees().add(employee); //adding the employee to the petStore Set<Employee>
		
		
		Employee dbEmployee = employeeDao.save(employee);
		return new PetStoreEmployee(dbEmployee);
		
	}
	
	
	
	
	private void copyEmployeeFields(Employee employee, PetStoreEmployee petStoreEmployee) {
		employee.setEmployeeId(petStoreEmployee.getEmployeeId());
		employee.setEmployeeFirstName(petStoreEmployee.getEmployeeFirstName());
		employee.setEmployeeLastName(petStoreEmployee.getEmployeeLastName());
		employee.setEmployeeJobTitle(petStoreEmployee.getEmployeeJobTitle());
		employee.setEmployeePhone(petStoreEmployee.getEmployeePhone());
	}




	private Employee findOrCreateEmployee(Long petStoreId, Long employeeId) {
		
		// If the employee ID is null, it should return a new Employee object.
		if(Objects.isNull(employeeId)) {
			return new Employee();
		}
		
		//If employee ID is NOT null - should call the method: findEmployeeByID()
		return findEmployeeById(petStoreId, employeeId);
		
	}



	//method: findEmployeeById
	public Employee findEmployeeById(Long petStoreId, Long employeeId) {
		
		Employee employee = employeeDao.findById(employeeId).orElseThrow(() -> new NoSuchElementException("Employee with ID=" + employeeId + " was not found."));
		
		 /* (Checks if the employee passed in matches with the petStore passed in)
		  * If the pet store ID in the Employee object's PetStore variable does not match the given pet store ID (passed in the parameter), 
		  * throw a new IllegalArgumentException. 
		  * (Each employee is assigned to a PetStore)
		*/
		if(employee.getPetStore().getPetStoreId() != petStoreId) {
			throw new IllegalArgumentException("The employee with ID=" + employeeId + " is not employed by the pet store with ID=" + petStoreId);
		}
		
		return employee;
	}

	@Transactional(readOnly = false)
	public PetStoreData savePetStore(PetStoreData petStoreData) {
		// get PetStoreId
		Long petStoreId = petStoreData.getPetStoreId();

		PetStore petStore = findOrCreatePetStore(petStoreId);

		//Matching fields are copied from the PetStoreData object to the PetStore object
		copyPetStoreFields(petStore, petStoreData);
		
		return new PetStoreData(petStoreDao.save(petStore));
	}

	private void copyPetStoreFields(PetStore petStore, PetStoreData petStoreData) {
		//Matching fields are copied from the PetStoreData object to the PetStore object
		
		petStore.setPetStoreId(petStoreData.getPetStoreId());
		petStore.setPetStoreName(petStoreData.getPetStoreName());		
		petStore.setPetStoreAddress(petStoreData.getPetStoreAddress());
		petStore.setPetStoreCity(petStoreData.getPetStoreCity());
		petStore.setPetStoreState(petStoreData.getPetStoreState());
		petStore.setPetStoreZip(petStoreData.getPetStoreZip());
		petStore.setPetStorePhone(petStoreData.getPetStorePhone());
		
		
	}

	private PetStore findOrCreatePetStore(Long petStoreId) {		
		// returns a new PetStore object if the pet store ID is null. 
		if(Objects.isNull(petStoreId)) {
			return new PetStore();
		} else {
			// If not null, the method should call findPetStoreById 
			return findPetStoreById(petStoreId);
		}
		
	}
	 
	
	private PetStore findPetStoreById(Long petStoreId) {
		//Returns a PetStore object if a pet store with matching ID exists in the database.
		return petStoreDao.findById(petStoreId)
				.orElseThrow(() -> new NoSuchElementException(
						"Pet store with ID=" + petStoreId + " not found."
						)); 
		//If no matching pet store is found, the method should throw a NoSuchElementException with an appropriate message.
	}









	

}
