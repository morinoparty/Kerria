import React from "react";
import { CommandLine } from "./command-line";
import { Command } from "../types/command";

interface CommandListProps {
    list: Command[];
}

const ranking = [
    "stable",
    "newly",
    "beta",
    "proposal",
    "deprecated"
]

// Create the badge component
export const CommandList: React.FC<CommandListProps> = ({ list }) => {
    return (
        <div>
            {ranking.map((rank) => (
                list
                    .filter(item => item.status === rank)
                    .map(item => (
                        <div key={item.command} className="pb-2">
                            <CommandLine
                                status={item.status}
                                command={item.command}
                                description={item.description}
                                aliases={item.aliases}
                                permission={item.permission}
                            />
                        </div>
                    ))
            ))}
        </div>
    );
};