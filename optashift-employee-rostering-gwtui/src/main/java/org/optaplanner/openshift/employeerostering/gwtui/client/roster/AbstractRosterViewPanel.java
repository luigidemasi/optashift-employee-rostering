package org.optaplanner.openshift.employeerostering.gwtui.client.roster;

import javax.inject.Inject;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.gwtbootstrap3.client.ui.Button;
import org.jboss.errai.common.client.dom.Span;
import org.jboss.errai.ui.client.local.api.IsElement;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.EventHandler;
import org.optaplanner.openshift.employeerostering.shared.roster.RosterRestServiceBuilder;

public abstract class AbstractRosterViewPanel implements IsElement {

    protected Integer tenantId = -1;

    @Inject @DataField
    protected Button solveButton;
    @Inject @DataField
    protected Button refreshButton;
    @Inject @DataField
    protected Span solveStatus;

    @EventHandler("refreshButton")
    public void refresh(ClickEvent e) {
        refresh();
    }

    public void refresh() {
        refreshTable();
    }

    protected abstract void refreshTable();

    @EventHandler("solveButton")
    public void solve(ClickEvent e) {
        RosterRestServiceBuilder.solveRoster(tenantId).send();
        // TODO 6 * 5000ms = 30 seconds - Keep in sync with solver config
        Scheduler.get().scheduleFixedDelay(new RefreshTableRepeatingCommand(6), 5000);
    }

    protected class RefreshTableRepeatingCommand implements Scheduler.RepeatingCommand {

        private int repeatCount;

        public RefreshTableRepeatingCommand(int repeatCount) {
            this.repeatCount = repeatCount;
            updateSolverStatus();
        }

        @Override
        public boolean execute() {
            refreshTable();
            // To repeat n times, return true only n-1 times.
            repeatCount--;
            if (repeatCount > 0) {
                updateSolverStatus();
                return true;
            } else {
                solveStatus.setInnerHTML(new SafeHtmlBuilder()
                        .appendHtmlConstant("Solving finished.")
                        .toSafeHtml().asString());
                return false;
            }
        }

        private void updateSolverStatus() {
            int remainingSeconds = 5000 * repeatCount / 1000;
            solveStatus.setInnerHTML(new SafeHtmlBuilder()
                    .appendHtmlConstant("Solving for another ")
                    .append(remainingSeconds)
                    .appendHtmlConstant(" seconds...")
                    .toSafeHtml().asString());
        }

    }
}
