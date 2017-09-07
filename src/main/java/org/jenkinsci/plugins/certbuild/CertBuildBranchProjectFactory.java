package org.jenkinsci.plugins.certbuild;

import hudson.Extension;
import hudson.model.TaskListener;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.multibranch.AbstractWorkflowBranchProjectFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;

public class CertBuildBranchProjectFactory extends AbstractWorkflowBranchProjectFactory {

    private String script;
    private boolean sandbox;
    private String markerFile;

    @DataBoundConstructor
    public CertBuildBranchProjectFactory() {
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
        return new CertBuildFlowDefinition(this.script, this.sandbox);
    }

    @Override public SCMSourceCriteria getSCMSourceCriteria(SCMSource source) {
        return new SCMSourceCriteria() {
            @Override public boolean isHead(SCMSourceCriteria.Probe probe, TaskListener listener) throws IOException {
                return probe.exists(markerFile);
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
