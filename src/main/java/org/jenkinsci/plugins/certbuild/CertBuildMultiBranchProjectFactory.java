package org.jenkinsci.plugins.certbuild;

import hudson.Extension;
import jenkins.branch.MultiBranchProjectFactory;
import jenkins.branch.MultiBranchProjectFactoryDescriptor;
import jenkins.scm.api.SCMSource;
import jenkins.scm.api.SCMSourceCriteria;
import org.jenkinsci.plugins.workflow.multibranch.AbstractWorkflowMultiBranchProjectFactory;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.io.IOException;

public class CertBuildMultiBranchProjectFactory extends AbstractWorkflowMultiBranchProjectFactory {

    private String markerFile;
    private String script;
    private boolean sandbox;

    @DataBoundConstructor
    public CertBuildMultiBranchProjectFactory() {
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

    @Override protected SCMSourceCriteria getSCMSourceCriteria(SCMSource source) {
        return newProjectFactory().getSCMSourceCriteria(source);
    }

    private CertBuildBranchProjectFactory newProjectFactory() {
        CertBuildBranchProjectFactory factory = new CertBuildBranchProjectFactory();
        factory.setSandbox(sandbox);
        factory.setScript(script);
        factory.setMarkerFile(markerFile);
        return factory;
    }
    
    @Extension public static class DescriptorImpl extends MultiBranchProjectFactoryDescriptor {

        @Override public MultiBranchProjectFactory newInstance() {
            return new CertBuildMultiBranchProjectFactory();
        }

        @Override public String getDisplayName() {
            return "Common pipeline definition for markerfile";
        }

    }

    @Override
    protected void customize(WorkflowMultiBranchProject project) throws IOException, InterruptedException {
        project.setProjectFactory(newProjectFactory());
    }
}
