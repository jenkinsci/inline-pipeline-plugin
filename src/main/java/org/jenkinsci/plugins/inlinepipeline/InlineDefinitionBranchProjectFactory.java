package org.jenkinsci.plugins.inlinepipeline;

import hudson.Extension;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.multibranch.AbstractWorkflowBranchProjectFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

public class InlineDefinitionBranchProjectFactory extends AbstractWorkflowBranchProjectFactory {

    private String script;
    private boolean sandbox;
    private String markerFile;
    private int maxDaysOld = 0;

    @DataBoundConstructor
    public InlineDefinitionBranchProjectFactory() {
    }

    @DataBoundSetter
    public void setMaxDaysOld(int maxDaysOld) {
        this.maxDaysOld = maxDaysOld;
    }

    public int getMaxDaysOld() {
        return maxDaysOld;
    }

    @DataBoundSetter
    public void setScript(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }

    @DataBoundSetter
    public void setSandbox(boolean sandbox) {
        this.sandbox = sandbox;
    }

    public boolean getSandbox() {
        return sandbox;
    }

    public String getMarkerFile() {
        return markerFile;
    }

    @DataBoundSetter
    public void setMarkerFile(String markerFile) {
        this.markerFile = markerFile;
    }

    @Override protected FlowDefinition createDefinition() {
        return new InlineFlowDefinition(this.script, this.sandbox);
    }

    @Override public SCMSourceCriteria getSCMSourceCriteria(SCMSource source) {
        return new SCMSourceCriteria() {
            @Override public boolean isHead(SCMSourceCriteria.Probe probe, TaskListener listener) throws IOException {
                long lastModified = probe.lastModified();
                long daysOld = TimeUnit.MILLISECONDS.toDays(new Date().getTime() - lastModified);

                if (probe.stat(markerFile).exists() && (
                        lastModified == 0 ||
                        maxDaysOld == 0 ||
                        maxDaysOld > daysOld)) {
                    return true;
                }

                return false;
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends AbstractWorkflowBranchProjectFactoryDescriptor {

        @Override public String getDisplayName() {
            return "Common pipeline definition for markerfile";
        }
    }
}
