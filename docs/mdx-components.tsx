import type { MDXComponents } from "mdx/types";
import defaultComponents from "fumadocs-ui/mdx";
import { Mermaid } from "./components/mdx/mermaind";
import { CommandList } from "./components/mdx/command-list";
import { CommandLine } from "./components/mdx/command-line";
import { UUIDV4, UUIDV7 } from "./components/mdx/uuid-example";
import { Timestamp } from "./components/mdx/timestamp";

const customComponents = {
    CommandList,
    CommandLine,
    UUIDV4,
    UUIDV7,
    Timestamp,
};

export function getMDXComponents(components?: MDXComponents): MDXComponents {
    return {
        ...defaultComponents,
        Mermaid,
        ...customComponents,
        ...components,
    };
}

export function useMDXComponents(components: MDXComponents): MDXComponents {
    return {
        ...defaultComponents,
        Mermaid,
        ...customComponents,
        ...components,
    };
}
