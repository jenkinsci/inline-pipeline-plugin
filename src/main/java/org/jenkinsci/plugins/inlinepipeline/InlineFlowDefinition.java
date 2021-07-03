package org.jenkinsci.plugins.inlinepipeline;

import hudson.Extension;
import hudson.Util;
import hudson.model.*;
import jenkins.branch.Branch;
import jenkins.scm.api.*;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinitionDescriptor;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.jenkinsci.plugins.workflow.multibranch.Messages;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.util.List;

class InlineFlowDefinition extends FlowDefinition {

    private String script;
    private boolean sandbox;
    private String markerFile;
    private String markerFileContent;

    @Deprecated
    public InlineFlowDefinition(String script, boolean sandbox) {
        this.script = script;
        this.sandbox = sandbox;
    }

    public InlineFlowDefinition(String script, boolean sandbox, String markerFile) {
        this(script, sandbox);
        this.markerFile = markerFile;
    }

    @Override public FlowExecution create(FlowExecutionOwner handle, TaskListener listener, List<? extends Action> actions) throws Exception {
        // For backward compatibility - seems like jobs configured prior to that feature will never have this field set until reconfigured
        if (this.markerFile != null) {
            Queue.Executable exec = handle.getExecutable();
            if (!(exec instanceof WorkflowRun)) {
                throw new IllegalStateException("inappropriate context");
            }
            WorkflowRun build = (WorkflowRun) exec;
            WorkflowJob job = build.getParent();
            BranchJobProperty property = job.getProperty(BranchJobProperty.class);
            if (property == null) {
                throw new IllegalStateException("inappropriate context");
            }
            Branch branch = property.getBranch();
            ItemGroup<?> parent = job.getParent();
            if (!(parent instanceof WorkflowMultiBranchProject)) {
                throw new IllegalStateException("inappropriate context");
            }
            SCMSource scmSource = ((WorkflowMultiBranchProject) parent).getSCMSource(branch.getSourceId());
            if (scmSource == null) {
                throw new IllegalStateException(branch.getSourceId() + " not found");
            }
            SCMHead head = branch.getHead();
            SCMRevision tip = scmSource.fetch(head, listener);
            if (tip == null) {
                throw new IllegalStateException(head.getName() + " not found");
            }

            build.addAction(new SCMRevisionAction(scmSource, tip));

            SCMRevision rev = scmSource.getTrustedRevision(tip, listener);
            if (!rev.equals(tip)) {
                throw new IllegalStateException(Messages.ReadTrustedStep__has_been_modified_in_an_untrusted_revis(markerFile));
            }

            SCMFileSystem fs = SCMFileSystem.of(scmSource, head, rev);
            if (fs == null) {
                throw new IllegalStateException("SCMFileSystem not found");
            }

            this.markerFileContent = fs.child(markerFile).contentAsString();
            listener.getLogger().println("Obtained " + markerFile + " from " + rev);
        }

        return new CpsFlowDefinition(Util.fixNull(this.script), this.sandbox).create(handle, listener, actions);
    }

    public String getMarkerFileContent() {
        return markerFileContent;
    }

    @Extension
    public static class DescriptorImpl extends FlowDefinitionDescriptor {

        @Override public String getDisplayName() {
            return "Inline pipeline definition from multibranch";
        }

    }

    /** Want to display this in the r/o configuration for a branch project, but not offer it on standalone jobs or in any other context. */
    @Extension
    public static class HideMeElsewhere extends DescriptorVisibilityFilter {

        @SuppressWarnings("rawtypes")
        @Override public boolean filter(Object context, Descriptor descriptor) {
            if (descriptor instanceof DescriptorImpl) {
                return context instanceof WorkflowJob && ((WorkflowJob) context).getParent() instanceof WorkflowMultiBranchProject;
            }
            return true;
        }

    }

}