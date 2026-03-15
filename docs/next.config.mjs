import { createMDX } from "fumadocs-mdx/next";

const withMDX = createMDX();

/** @type {import('next').NextConfig} */
const config = {
    reactStrictMode: true,
    output: "export",
    trailingSlash: true,
    basePath: process.env.BASE_PATH || "",
    env: {
        NEXT_PUBLIC_BASE_PATH: process.env.BASE_PATH || "",
    },
    images: {
        unoptimized: true,
    },
};

export default withMDX(config);
