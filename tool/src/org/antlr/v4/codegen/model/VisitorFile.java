/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Terence Parr
 *  Copyright (c) 2012 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.antlr.v4.codegen.model;

import org.antlr.runtime.RecognitionException;
import org.antlr.v4.codegen.OutputModelFactory;
import org.antlr.v4.runtime.misc.Tuple2;
import org.antlr.v4.tool.Grammar;
import org.antlr.v4.tool.Rule;
import org.antlr.v4.tool.ast.ActionAST;
import org.antlr.v4.tool.ast.AltAST;
import org.antlr.v4.tool.ast.RuleAST;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisitorFile extends OutputFile {
	public String genPackage; // from -package cmd-line
	public String grammarName;
	public String parserName;
	/**
	 * The names of all rule contexts which may need to be visited.
	 */
	public Set<String> visitorNames = new HashSet<String>();
	/**
	 * For rule contexts created for a labeled outer alternative, maps from
	 * a listener context name to the name of the rule which defines the
	 * context.
	 */
	public Map<String, String> visitorLabelRuleNames = new HashMap<String, String>();

	@ModelElement public Action header;

	public VisitorFile(OutputModelFactory factory, String fileName) {
		super(factory, fileName);
		Grammar g = factory.getGrammar();
		parserName = g.getRecognizerName();
		grammarName = g.name;

		for (Map.Entry<String, List<RuleAST>> entry : g.contextASTs.entrySet()) {
			for (RuleAST ruleAST : entry.getValue()) {
				try {
					Map<String, List<Tuple2<Integer, AltAST>>> labeledAlternatives = g.getLabeledAlternatives(ruleAST);
					visitorNames.addAll(labeledAlternatives.keySet());
				} catch (RecognitionException ex) {
				}
			}
		}

		for (Rule r : g.rules.values()) {
			visitorNames.add(r.getBaseContext());
		}

		for (Rule r : g.rules.values()) {
			Map<String, List<Tuple2<Integer,AltAST>>> labels = r.getAltLabels();
			if ( labels!=null ) {
				for (Map.Entry<String, List<Tuple2<Integer, AltAST>>> pair : labels.entrySet()) {
					visitorLabelRuleNames.put(pair.getKey(), r.name);
				}
			}
		}

		ActionAST ast = g.namedActions.get("header");
		if ( ast!=null ) header = new Action(factory, ast);
		genPackage = factory.getGrammar().tool.genPackage;
	}
}
