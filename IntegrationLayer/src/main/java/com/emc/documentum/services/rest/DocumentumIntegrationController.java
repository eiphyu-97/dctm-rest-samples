package com.emc.documentum.services.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.emc.documentum.delegate.provider.APIDelegateProvider;
import com.emc.documentum.delegates.DocumentumDelegate;
import com.emc.documentum.dtos.DocumentumDocument;
import com.emc.documentum.dtos.DocumentumFolder;
import com.emc.documentum.dtos.DocumentumObject;
import com.emc.documentum.dtos.DocumentumProperty;
import com.emc.documentum.exceptions.CanNotDeleteFolderException;
import com.emc.documentum.exceptions.DelegateNotFoundException;
import com.emc.documentum.exceptions.DocumentCheckoutException;
import com.emc.documentum.exceptions.DocumentumException;
import com.emc.documentum.exceptions.ObjectNotFoundException;
import com.emc.documentum.exceptions.RepositoryNotAvailableException;
import com.emc.documentum.translation.TranslationUtility;

import io.swagger.annotations.ApiOperation;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

@RestController
@RequestMapping("{api}/services")
@CrossOrigin("*")
public class DocumentumIntegrationController extends BaseController {

	Logger log = Logger.getLogger(DocumentumIntegrationController.class.getCanonicalName());

	@Autowired
	APIDelegateProvider delegateProvider;

	@Autowired
	TranslationUtility translationUtility;

	@ApiOperation(value = "Create Folder", notes = "Create a folder named {folderName} under the folder/cabinet identified using {parentId}")
	@RequestMapping(value = "/folder/create/{parentId}/{folderName}", method = RequestMethod.POST)
	public DocumentumFolder createFolderUsingParentId(@PathVariable(value = "api") String api,
			@PathVariable(value = "parentId") String parentId, @PathVariable(value = "folderName") String folderName)
			throws DocumentumException, DelegateNotFoundException {
		try {
			return delegateProvider.getDelegate(api).createFolderByParentId(parentId, folderName);
		} catch (DocumentumException e) {
			// TODO Customize Error Handling
			throw e;
		}
	}

	@ApiOperation(value = "Create Folder under a parent", notes = "Create a folder under a parent identifed by {parentId}", hidden = true)
	@RequestMapping(value = "/folder/create/{parentId}", method = RequestMethod.POST)
	public DocumentumFolder createFolder(@PathVariable(value = "api") String api,
			@PathVariable(value = "parentId") String parentId, @RequestBody HashMap<String, Object> properties)
			throws DocumentumException, DelegateNotFoundException {
		try {
			return delegateProvider.getDelegate(api).createFolder(parentId, properties);
		} catch (DocumentumException e) {
			// TODO Customize Error Handling
			throw e;
		}
	}

	// @ApiOperation(value = "Create Document", notes = "Create a Contentless
	// document")
	// @RequestMapping(value = "/document/create", method = RequestMethod.POST)
	// public DocumentumDocument createDocument(@PathVariable(value = "api")
	// String api,
	// @Valid @RequestBody DocumentCreation docCreation) throws
	// DocumentumException, DelegateNotFoundException {
	// try {
	// return (delegateProvider.getDelegate(api)).createDocument(docCreation);
	// } catch (DocumentumException e) {
	// // TODO Customize Error Handling
	// throw e;
	// }
	// }

	@ApiOperation(value = "Create Document", notes = "Create a Contentless document")
	@RequestMapping(value = "/folder/{folderId}/document", method = RequestMethod.POST)
	public DocumentumDocument createDocument(@PathVariable(value = "api") String api,
			@Valid @RequestBody DocumentumDocument document, @PathVariable(value = "folderId") String folderId)
			throws DocumentumException, DelegateNotFoundException {
		translationUtility.translateToRepo(document, api);
		DocumentumDocument createdDocument = (delegateProvider.getDelegate(api)).createDocument(folderId, document);
		translationUtility.translateFromRepo(createdDocument, api);
		return createdDocument;
	}

	@ApiOperation(value = "Get Cabinet By Name", notes = "Get a Cabinet by its name")
	@RequestMapping(value = "get/cabinet/name/{cabinetName}", method = RequestMethod.GET)
	public DocumentumFolder getCabinetByName(@PathVariable(value = "api") String api,
			@PathVariable(value = "cabinetName") String cabinetName)
			throws DelegateNotFoundException, DocumentumException {

		try {
			DocumentumDelegate delegate = delegateProvider.getDelegate(api);
			DocumentumFolder cabinet = delegate.getCabinetByName(cabinetName);
			translationUtility.translateFromRepo(cabinet, api);
			return cabinet;
		} catch (ObjectNotFoundException e) {
			// TODO Auto-generated catch block
			throw e;
		}

	}

	@ApiOperation(value = "Get Object By Id", notes = "Get an object by its object_id")
	@RequestMapping(value = "get/object/id/{objectId}", method = { RequestMethod.GET })
	public DocumentumObject getCabinetById(@PathVariable(value = "api") String api,
			@PathVariable(value = "objectId") String objectId)
			throws ObjectNotFoundException, RepositoryNotAvailableException, DelegateNotFoundException {
		return (delegateProvider.getDelegate(api)).getObjectById(objectId);

	}

	@ApiOperation(value = "Delete Object", notes = "Deletes an object using identified by its {objectId}, if the object is a folder with children the deleteChildren query paramater must be set to true")
	@RequestMapping(value = "delete/object/id/{objectId}", method = { RequestMethod.DELETE })
	public void deleteObject(@PathVariable(value = "api") String api, @PathVariable(value = "objectId") String objectId,
			@RequestParam(name = "deleteChildren", defaultValue = "false") boolean deleteChildren)
			throws ObjectNotFoundException, RepositoryNotAvailableException, DelegateNotFoundException,
			CanNotDeleteFolderException {
		try {
			(delegateProvider.getDelegate(api)).deleteObject(objectId, deleteChildren);
		} catch (CanNotDeleteFolderException e) {
			e.printStackTrace();
			throw e;
		}
		return;

	}

	@ApiOperation(value = "Get Cabinets", notes = "Get all Cabinets")
	@RequestMapping(value = "get/cabinets", method = RequestMethod.GET)
	public ArrayList<DocumentumFolder> getAllCabinets(@PathVariable(value = "api") String api,
			@RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "20") int pageSize)
			throws RepositoryNotAvailableException, DelegateNotFoundException {
		return (delegateProvider.getDelegate(api)).getAllCabinets(pageNumber, pageSize);
	}

	@ApiOperation(value = "Get Children of a Folder", notes = "Get children of a folder/cabinet identified by its {folderId}")
	@RequestMapping(value = "get/{folderId}/children", method = RequestMethod.GET)
	public ArrayList<DocumentumObject> getChildren(@PathVariable(value = "api") String api,
			@PathVariable(value = "folderId") String folderId,
			@RequestParam(name = "pageNumber", defaultValue = "1") int pageNumber,
			@RequestParam(name = "pageSize", defaultValue = "20") int pageSize) throws Exception {
		System.out.println("Page Number: " + pageNumber + " Page Size: " + pageSize);
		return (delegateProvider.getDelegate(api)).getChildren(folderId, pageNumber, pageSize);
	}

	@ApiOperation(value = "Get Document Content", notes = "Gets the document content as a Base64 encoded string")
	@RequestMapping(value = "get/document/content/id/{documentId}", method = RequestMethod.GET)
	public Object getDocumentContentById(@PathVariable(value = "api") String api,
			@PathVariable(value = "documentId") String documentId)
			throws ObjectNotFoundException, RepositoryNotAvailableException, DelegateNotFoundException {
		return (delegateProvider.getDelegate(api)).getDocumentContentById(documentId);
	}

	@ApiOperation(value = "Search document by name", notes = "Search document by its name")
	@RequestMapping(value = "document/search/{name}", method = RequestMethod.GET)
	public ArrayList<DocumentumObject> searchDocumentByName(@PathVariable(value = "api") String api,
			@PathVariable(value = "name") String name)
			throws RepositoryNotAvailableException, DelegateNotFoundException {
		log.entering("searchDocumentByName", name);
		return (delegateProvider.getDelegate(api)).getDocumentByName(name);
	}

	@ApiOperation(value = "Checkout Document", notes = "Checkout a specific document")
	@RequestMapping(value = "get/document/checkout/id/{documentId}", method = RequestMethod.POST)
	public DocumentumDocument checkoutDocument(@PathVariable(value = "api") String api,
			@PathVariable(value = "documentId") String documentId)
			throws DelegateNotFoundException, DocumentumException {
		log.entering("checkout document ", documentId);
		return (delegateProvider.getDelegate(api)).checkoutDocument(documentId);
	}

	@ApiOperation(value = "Checkin Document", notes = "Check in a document using the provided Base64 encoded stream")
	@RequestMapping(value = "get/document/checkin/id/{documentId}", method = RequestMethod.POST)
	public DocumentumDocument checkinDocument(@PathVariable(value = "api") String api,
			@PathVariable(value = "documentId") String documentId, @RequestBody byte[] content)
			throws DelegateNotFoundException, DocumentumException {
		log.entering("checkin document ", documentId);
		return (delegateProvider.getDelegate(api)).checkinDocument(documentId, content);
	}

	@ApiOperation(value = "Cancel Document Checkout", notes = "Cancels the Checkout of this specific document")
	@RequestMapping(value = "get/document/cancelCheckout/id/{documentId}", method = RequestMethod.POST)
	public DocumentumObject cancelCheckout(@PathVariable(value = "api") String api,
			@PathVariable(value = "documentId") String documentId)
			throws RepositoryNotAvailableException, DocumentCheckoutException, DelegateNotFoundException {
		log.entering("checkin document ", documentId);
		return (delegateProvider.getDelegate(api)).cancelCheckout(documentId);
	}

	@ApiOperation(value = "Get Object Properties", notes = "Gets the properties of a specific object")
	@RequestMapping(value = "get/object/properties/id/{objectId}", method = RequestMethod.GET)
	public ArrayList<DocumentumProperty> GetObjectProperties(@PathVariable(value = "api") String api,
			@PathVariable(value = "objectId") String objectId)
			throws RepositoryNotAvailableException, DocumentCheckoutException, DelegateNotFoundException {
		log.entering("Getting object properties ", objectId);
		return (delegateProvider.getDelegate(api)).getObjectProperties(objectId);
	}

	@ApiOperation(value = "Get document annotations", notes = "Gets the annotations of a specific document")
	@RequestMapping(value = "get/document/annotations/id/{documentId}/page/{pageNumber}", method = RequestMethod.GET)
	public ArrayList<DocumentumObject> GetDocumentAnnotations(@PathVariable(value = "api") String api,
			@PathVariable(value = "documentId") String documentId,@PathVariable(value = "pageNumber") int pageNumber)
			throws DocumentumException, DelegateNotFoundException {
		log.entering("Getting document annotations ", documentId);
		return (delegateProvider.getDelegate(api)).getDocumentRelationsByRelationName(documentId , "DM_ANNOTATE",pageNumber);
	}

	@ApiOperation(value = "create document annotation", notes = "creates annotation for a specific document")
	@RequestMapping(value = "/document/{documentId}/annotations", method = RequestMethod.POST, consumes = {
			"multipart/form-data" })
	public DocumentumObject createDocumentAnnotations(@PathVariable(value = "api") String api,
			@PathVariable(value = "documentId") String documentId,
			@RequestPart("properties") HashMap<String, Object> properties, @RequestPart("binary") byte[] file)
			throws DocumentumException, DelegateNotFoundException {
		log.entering("creating document annotation ", documentId);
			return (delegateProvider.getDelegate(api)).createDocumentAnnotation(documentId, file,
					properties);
		}

	@ApiOperation(value = "get document renditions", notes = "gets renditions for a specific document")
	@RequestMapping(value = "get/document/{documentId}/renditions", method = RequestMethod.GET)
	public ArrayList<DocumentumObject> getDocumentRenditions(@PathVariable(value = "api") String api,
			@PathVariable(value = "documentId") String documentId)
			throws DocumentumException, DelegateNotFoundException {
		log.entering("getting  document renditions ", documentId);
		return (delegateProvider.getDelegate(api)).getRenditionsByDocumentId(documentId);
	}

	@RequestMapping(value = "object/{objectId}/rename", method = RequestMethod.POST)
	public DocumentumObject renameObject(@PathVariable(value = "api") String api,
			@PathVariable(value = "objectId") String objectId, @RequestBody String newName)
			throws ObjectNotFoundException, DelegateNotFoundException, RepositoryNotAvailableException {
		return (delegateProvider.getDelegate(api)).renameObject(objectId, newName);
	}
	@RequestMapping(value = "object/{objectId}/move", method = RequestMethod.POST)
	public DocumentumObject moveObject(@PathVariable(value = "api") String api,
			@PathVariable(value = "objectId") String objectId, @RequestBody String targetFolderId)
			throws DelegateNotFoundException, DocumentumException {
		return (delegateProvider.getDelegate(api)).moveObject(objectId, targetFolderId);
	}
	
	@RequestMapping(value = "object/{objectId}/copy", method = RequestMethod.POST)
	public DocumentumObject copyObject(@PathVariable(value = "api") String api,
			@PathVariable(value = "objectId") String objectId, @RequestBody String targetFolderId)
			throws DelegateNotFoundException, DocumentumException {
		return (delegateProvider.getDelegate(api)).copyObject(objectId, targetFolderId);
	}

	
	
	
	@RequestMapping(value = "document/{documentId}/comment/{comment}/userName/{userName}", method = RequestMethod.POST)
	public String addCommentToDocument(@PathVariable(value="documentId")String documentId , @PathVariable(value="comment")String comment , @PathVariable(value="userName")String userName , @PathVariable(value = "api") String api)
			throws DocumentumException, DelegateNotFoundException {
		log.entering("adding comment to document ", documentId);
		try {
			DocumentumDelegate dcDelegate = delegateProvider.getDelegate(api) ;
			DocumentumObject commentObject = dcDelegate.addCommentToDocument(documentId, userName+","+comment);
			String date = (String) commentObject.getPropertiesAsMap().get("r_creation_date") ;

			JSONArray children = new JSONArray() ;
			JSONObject json = new JSONObject() ;
			json.put("user", userName) ;
			json.put("content", comment) ;
			json.put("date", date) ;
			children.add(json) ;
			JSONObject returnJson = new JSONObject() ;
			returnJson.put("result", children) ;
			return returnJson.toString() ;
		} catch (DelegateNotFoundException e) {
			e.printStackTrace();
			return errorResponse(api + " Repository is not available ") ;
		}	
	}
	
	@RequestMapping(value = "document/{documentId}/comments", method = RequestMethod.GET)
	public String getDocumentComments(@PathVariable(value="documentId")String documentId ,@PathVariable(value = "api") String api){
		log.entering("getting  document comments ", documentId);
			try {
				DocumentumDelegate dcDelegate = delegateProvider.getDelegate(api) ;	
				//TODO add relation name 
				ArrayList<DocumentumObject> comments = dcDelegate.getDocumentComments(documentId, "dm_wf_email_template") ;
				return getDocumentComments(comments) ;
			} catch (DelegateNotFoundException e) {
				e.printStackTrace();
				return errorResponse(api + " Repository is not available ") ;
			} catch (DocumentumException e) {
				e.printStackTrace();
				return errorResponse("Documentum exception ... ") ;
			}
	}
	
	private String getDocumentComments(ArrayList<DocumentumObject> objects)
	{
		JSONArray children = new JSONArray() ;
		for (int i = 0 ; i < objects.size() ; i++) {
			JSONObject json = new JSONObject() ;
			ArrayList<DocumentumProperty> properties = objects.get(i).getProperties();
			String comment = null ;
			String date = null ;
			String[] array = null ;
			for(DocumentumProperty property : properties){
				if(property.getLocalName().equals("content")){
						comment = (String) property.getValue() ;
						array = comment.split(",") ;
						if(array.length == 2)
						{
							json.put("user", array[0]) ;
							json.put("content", array[1]) ;
						}
						else //case of user was not stored ...
						{
							json.put("user", "unknown") ;
							json.put("content", array[0]) ;
						}
				}
				else if(property.getLocalName().equals("date"))
				{
					date = (String) property.getValue() ;
					json.put("date", date) ;
				}
				
			}
			children.add(json) ;
		}
		JSONObject returnJson = new JSONObject() ;
		returnJson.put("result", children) ;
		return returnJson.toString() ;
	}
	
}