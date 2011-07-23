/**
 * Aptana Studio
 * Copyright (c) 2005-2011 by Appcelerator, Inc. All Rights Reserved.
 * Licensed under the terms of the GNU Public License (GPL) v3 (with exceptions).
 * Please see the license.html included with this distribution for details.
 * Any modifications to this file must keep this entire header intact.
 */
package com.aptana.parsing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import beaver.Parser;
import beaver.Parser.Simulator;
import beaver.Parser.TokenStream;
import beaver.Symbol;

import com.aptana.parsing.lexer.ITypePredicate;

/**
 * InsertionRecoveryStrategy
 */
public class InsertionRecoveryStrategy<T extends ITypePredicate> implements IRecoveryStrategy
{
	private class CandidateToken
	{
		public final T tokenType;
		public final String tokenText;

		public CandidateToken(T tokenType, String tokenText)
		{
			this.tokenType = tokenType;
			this.tokenText = tokenText;
		}
	}

	private List<CandidateToken> candidateTokens = new ArrayList<CandidateToken>();
	private Set<Short> lastTypes = new HashSet<Short>();
	private Set<Short> currentTypes = new HashSet<Short>();

	/**
	 * InsertionRecoveryStrategy
	 * 
	 * @param type
	 * @param text
	 * @param lastTokenTypes
	 */
	public InsertionRecoveryStrategy(T type, String text, T... lastTokenTypes)
	{
		addToken(type, text);
		addLastTokenTypes(lastTokenTypes);
	}

	/**
	 * InsertionRecoveryStrategy
	 * 
	 * @param type1
	 * @param text1
	 * @param type2
	 * @param text2
	 * @param lastTokenTypes
	 */
	public InsertionRecoveryStrategy(T type1, String text1, T type2, String text2, T... lastTokenTypes)
	{
		addToken(type1, text1);
		addToken(type2, text2);
		addLastTokenTypes(lastTokenTypes);
	}

	/**
	 * addCurrentTokenTypes
	 * 
	 * @param tokenTypes
	 */
	public void addCurrentTokenTypes(T... tokenTypes)
	{
		for (T tokenType : tokenTypes)
		{
			currentTypes.add(tokenType.getIndex());
		}
	}

	/**
	 * addLastTokenTypes
	 * 
	 * @param tokenTypes
	 */
	public void addLastTokenTypes(T... tokenTypes)
	{
		for (T tokenType : tokenTypes)
		{
			lastTypes.add(tokenType.getIndex());
		}
	}

	/**
	 * addToken
	 * 
	 * @param tokenType
	 * @param text
	 */
	public void addToken(T tokenType, String text)
	{
		candidateTokens.add(new CandidateToken(tokenType, text));
	}

	/*
	 * (non-Javadoc)
	 * @see com.aptana.parsing.IRecoveryStrategy#recover(com.aptana.parsing.IParser, beaver.Symbol, beaver.Symbol,
	 * beaver.Parser.TokenStream)
	 */
	public boolean recover(IParser parser, Symbol lastToken, Symbol currentToken, TokenStream in) throws IOException
	{
		boolean result = false;

		if (candidateTokens.size() > 0 && (lastTypes.size() == 0 || lastTypes.contains(lastToken.getId()))
				&& (currentTypes.size() == 0 || currentTypes.contains(currentToken.getId())))
		{
			// allocate room for all tokens we're going to try
			in.alloc(candidateTokens.size() + 1);

			// insert the token that failed
			in.insert(currentToken);

			// create a list of tokens we want to try
			List<Symbol> terminals = new ArrayList<Symbol>(candidateTokens.size());
			int tokenStart = currentToken.getStart();
			int tokenEnd = tokenStart - 1;

			for (CandidateToken candidateToken : candidateTokens)
			{
				short id = candidateToken.tokenType.getIndex();
				String text = candidateToken.tokenText;
				Symbol term = new Symbol(id, tokenStart, tokenEnd, text);

				terminals.add(term);
			}

			Collections.reverse(terminals);

			// create a new simulator to test our updated token stream
			if (parser instanceof Parser)
			{
				Simulator sim = ((Parser) parser).new Simulator();

				// insert test tokens into stream
				for (Symbol terminal : terminals)
				{
					in.insert(terminal);
				}

				// try it out and see what happens
				if (sim.parse(in))
				{
					result = true;

					// reset the stream so the parse can pick up from where it left off, but with our new tokens
					// included
					in.rewind();

					// report what tokens were added
					// for (Symbol terminal : terminals)
					// {
					// report.misssingTokenInserted(terminal);
					// }
				}
			}
		}

		return result;
	}
}
