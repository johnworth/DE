package org.iplantc.de.analysis.client.views.widget;

import org.iplantc.de.analysis.client.events.SaveAnalysisParametersEvent;
import org.iplantc.de.client.models.analysis.AnalysisParameter;
import org.iplantc.de.client.services.FileEditorServiceFacade;
import org.iplantc.de.commons.client.views.gxt3.dialogs.IPlantDialog;
import org.iplantc.de.diskResource.client.views.dialogs.SaveAsDialog;
import org.iplantc.de.resources.client.messages.IplantDisplayStrings;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer;
import com.sencha.gxt.widget.core.client.container.BorderLayoutContainer.BorderLayoutData;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent.SelectHandler;
import com.sencha.gxt.widget.core.client.grid.ColumnModel;
import com.sencha.gxt.widget.core.client.grid.Grid;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;

import java.util.List;

/**
 * FIXME JDS Fix debug ids.
 */
public class AnalysisParamView implements IsWidget, SaveAnalysisParametersEvent.HasSaveAnalysisParametersEventHandlers {

    private static AnalysisParamViewUiBinder uiBinder = GWT.create(AnalysisParamViewUiBinder.class);

    interface AnalysisParamViewUiBinder extends UiBinder<Widget, AnalysisParamView> {
    }

    @UiField(provided = true)
    final ListStore<AnalysisParameter> listStore;
    private final IplantDisplayStrings displayStrings;
    private final FileEditorServiceFacade fileEditorService;

    @UiField(provided = true)
    final ColumnModel<AnalysisParameter> cm;

    @UiField
    Grid<AnalysisParameter> grid;

    @UiField
    BorderLayoutContainer con;

    @UiField
    ToolBar menuToolBar;

    @UiField
    BorderLayoutData northData;

    @UiField
    IPlantDialog dialog;

    @UiField
    TextButton btnSave;

    private final Widget widget;

    public AnalysisParamView(ListStore<AnalysisParameter> listStore, ColumnModel<AnalysisParameter> cm, IplantDisplayStrings displayStrings, final FileEditorServiceFacade fileEditorService) {
        this.cm = cm;
        this.listStore = listStore;
        this.displayStrings = displayStrings;
        this.fileEditorService = fileEditorService;
        this.widget = uiBinder.createAndBindUi(this);
        grid.getView().setEmptyText(displayStrings.noParameters());
    }

    @Override
    public HandlerRegistration addSaveAnalysisParametersEventHandler(SaveAnalysisParametersEvent.SaveAnalysisParametersEventHandler handler) {
        return widget.addHandler(handler, SaveAnalysisParametersEvent.TYPE);
    }

    @Override
    public Widget asWidget() {
        return widget;
    }

    public void loadParameters(List<AnalysisParameter> items) {
        listStore.addAll(items);
    }

    public void show() {
        dialog.show();
    }

    public void setHeading(String heading) {
        dialog.setHeadingText(heading);
    }

    @UiHandler("btnSave")
    void onSaveClick(SelectEvent event) {
        final SaveAsDialog saveDialog = new SaveAsDialog();
        saveDialog.addOkButtonSelectHandler(new SelectHandler() {

            @Override
            public void onSelect(SelectEvent event) {
            	if(saveDialog.isVaild()) {
                String fileContents = writeTabFile();
                saveFile(saveDialog.getSelectedFolder().getPath() + "/" + saveDialog.getFileName(),
                        fileContents);
                saveDialog.hide();
            	}
            }
        });
        
        saveDialog.addCancelButtonSelectHandler(new SelectHandler() {
			
			@Override
			public void onSelect(SelectEvent event) {
				saveDialog.hide();
			}
		});
        saveDialog.show();
        saveDialog.toFront();
    }

    public void mask() {
        con.mask(displayStrings.loadingMask());
    }

    public void unmask() {
        con.unmask();
    }

    private void saveFile(final String path, String fileContents) {
        widget.fireEvent(new SaveAnalysisParametersEvent(path, fileContents));
    }

    private String writeTabFile() {
        StringBuilder sw = new StringBuilder();
        sw.append(displayStrings.paramName()).append("\t").append(displayStrings.paramType()).append("\t").append(displayStrings.paramValue()).append("\n");
        List<AnalysisParameter> params = grid.getStore().getAll();
        for (AnalysisParameter ap : params) {
            sw.append(ap.getName()).append("\t").append(ap.getType()).append("\t").append(ap.getDisplayValue()).append("\n");
        }

        return sw.toString();
    }

}
