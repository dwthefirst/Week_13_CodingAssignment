package pet.store.service;

import java.util.NoSuchElementException;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import pet.store.controller.model.PetStoreData;
import pet.store.dao.PetStoreDao;
import pet.store.entity.PetStore;

@Service
public class PetStoreService {

	// instance variable
	@Autowired
	private PetStoreDao petStoreDao;

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
