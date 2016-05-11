package org.iplantc.de.admin.desktop.client.ontologies.service;

import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.avu.Avu;
import org.iplantc.de.client.models.avu.AvuList;
import org.iplantc.de.client.models.ontologies.Ontology;
import org.iplantc.de.client.models.ontologies.OntologyHierarchy;
import org.iplantc.de.client.models.ontologies.OntologyMetadata;
import org.iplantc.de.client.models.ontologies.OntologyVersionDetail;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.List;

/**
 * @author aramsey
 */
public interface OntologyServiceFacade {

    /**
     * Save an ontology hierarchy at the given root to the specified ontology version
     * @param version
     * @param root
     * @param callback
     */
    void saveOntologyHierarchy(String version, String root, AsyncCallback<OntologyHierarchy> callback);

    /**
     * Get a list of all ontology versions that are available
     * @param callback
     */
    void getOntologies(AsyncCallback<List<Ontology>> callback);


    /**
     * Get the saved hierarchy at the given root IRI for the specified version of the ontology
     * @param callback
     */
    void getOntologyHierarchies(String version, AsyncCallback<List<OntologyHierarchy>> callback);

    /**
     * Get the list of apps that are not tagged with the given root for the specified Ontology version
     * @param version
     * @param root
     * @param callback
     */
    void getUnclassifiedApps(String version, String root, OntologyMetadata metadata, AsyncCallback<List<App>> callback);
    /**
     * Set the active ontology version used by the app-hierarchies endpoints
     * @param version
     * @param callback
     */
    void setActiveOntologyVersion(String version, AsyncCallback<OntologyVersionDetail> callback);

    /**
     * Get the list of apps that belong to the specified class iri and metadata attribute
     * @param iri
     * @param metadata
     * @param callback
     */
    void getAppsByHierarchy(String iri, OntologyMetadata metadata, AsyncCallback<List<App>> callback);

    /**
     * Add/Append a list of metadata tags to an App
     * @param app
     * @param avus
     * @param callback
     */
    void addAVUToApp(App app, AvuList avus, AsyncCallback<List<Avu>> callback);

    /**
     * Set the metadata tags for an App (overwriting any existing tags)
     * @param app
     * @param avus
     * @param callback
     */
    void setAppAVUs(App app, AvuList avus, AsyncCallback<List<Avu>> callback);

    /**
     * Get the list of metadata tags for an App
     * @param app
     * @param callback
     */
    void getAppAVUs(App app, AsyncCallback<List<Avu>> callback);
}
