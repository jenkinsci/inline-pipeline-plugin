package org.jenkinsci.plugins.certbuild;

import hudson.Extension;
import hudson.Util;
import hudson.model.Action;
import hudson.model.Descriptor;
import hudson.model.DescriptorVisibilityFilter;
import hudson.model.ItemGroup;
import hudson.model.Queue;
import hudson.model.TaskListener;
import jenkins.branch.Branch;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.flow.FlowDefinitionDescriptor;
import org.jenkinsci.plugins.workflow.flow.FlowExecution;
import org.jenkinsci.plugins.workflow.flow.FlowExecutionOwner;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.jenkinsci.plugins.workflow.multibranch.BranchJobProperty;
import org.jenkinsci.plugins.workflow.multibranch.WorkflowMultiBranchProject;

import java.util.List;

class CertBuildFlowDefinition extends FlowDefinition {

    private String script;
    private boolean sandbox;

    public CertBuildFlowDefinition(String script, boolean sandbox) {
        this.script = script;
        this.sandbox = sandbox;
    }

    @Override public FlowExecution create(FlowExecutionOwner handle, TaskListener listener, List<? extends Action> actions) throws Exception {
        return new CpsFlowDefinition(Util.fixNull(this.script), this.sandbox).create(handle, listener, actions);
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