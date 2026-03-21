"use client";

import type React from "react";
import type { Command } from "@/types/command";
import { CommandLine } from "./command-line";

interface CommandListProps {
    list: Command[];
}

const ranking = ["stable", "newly", "beta", "proposal", "deprecated"];

export const CommandList: React.FC<CommandListProps> = ({
    list,
}: CommandListProps) => {
    return (
        <div>
            {ranking.map((rank) =>
                list
                    .filter((item) => item.status === rank)
                    .map((item) => (
                        <div key={item.command} className="pb-2">
                            <CommandLine command={item} />
                        </div>
                    )),
            )}
        </div>
    );
};
