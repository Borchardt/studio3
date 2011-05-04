package com.aptana.editor.yaml.parsing;

import org.yaml.snakeyaml.nodes.ScalarNode;

import com.aptana.editor.yaml.IYAMLConstants;
import com.aptana.parsing.IParseState;
import com.aptana.parsing.ast.ParseNode;

public class ScalarParseNode extends ParseNode
{

	private ScalarNode node;

	public ScalarParseNode(ScalarNode node, IParseState parseState)
	{
		super(IYAMLConstants.CONTENT_TYPE_YAML);
		setLocation(YAMLParseRootNode.getStart(node, parseState), YAMLParseRootNode.getEnd(node, parseState));
		this.node = node;
	}

	@Override
	public String getText()
	{
		return node.getValue();
	}

}
