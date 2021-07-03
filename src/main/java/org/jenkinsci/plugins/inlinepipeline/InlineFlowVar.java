package org.jenkinsci.plugins.inlinepipeline;

import hudson.AbortException;
import hudson.Extension;
import hudson.model.Job;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.cps.CpsScript;
import org.jenkinsci.plugins.workflow.cps.GlobalVariable;
import org.jenkinsci.plugins.workflow.flow.FlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;

@Extension public class InlineFlowVar extends GlobalVariable {
    @Override public String getName() {
        return "markerFile";
    }

    @Override public String getValue(CpsScript script) throws Exception {
        Run<?,?> build = script.$build();
        if (!(build instanceof WorkflowRun)) {
            throw new AbortException("‘markerFile’ is not available outside a Pipeline build");
        }
        Job<?,?> job = build.getParent();
        FlowDefinition flow = ((WorkflowJob) job).getDefinition();
        if (!(flow instanceof InlineFlowDefinition)) {
            throw new AbortException("‘markerFile’ is not available if marker file recognizer was not used");
        }
        String file = ((InlineFlowDefinition) flow).getMarkerFileContent();
        if (file == null) {
            throw new AbortException("‘markerFile’ was not available");
        }
        return file;
    }
}
