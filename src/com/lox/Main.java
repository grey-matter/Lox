package com.lox;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.lox.TokenType.EOF;

public class Main {
    private static boolean hadError = false;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.print("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private static void runPrompt() throws IOException {

        InputStreamReader reader = new InputStreamReader(System.in);
        BufferedReader bf = new BufferedReader(reader);
        while (true) {
            System.out.println("> ");
            run(bf.readLine());
            hadError = false;
        }
    }

    private static void run(String source) {
        Scanner sc = new Scanner(source);
        List<Token> tokens = sc.scanTokens();

        for (Token token : tokens) {
            System.out.println(token);
        }
        Parser parser = new Parser(tokens);
        Expr expression = parser.parse();

        if (hadError)
            return;

        System.out.println(new AstPrinter().print(expression));
    }

    static void error(int line, String msg) {
        report(line, "", msg);
    }

    private static void report(int line, String where, String error) {
        System.err.println(
                "[line " + line + "] Error" + where + ": " + error);
        hadError = true;
    }

    static void error(Token token, String error) {
        if (token.tokenType == EOF) {
            report(token.line, " at end", error);
        } else {
            report(token.line, " at '" + token.lexeme + "'", error);
        }
    }

    private static void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes));

        if (hadError)
            System.exit(65);
    }
}