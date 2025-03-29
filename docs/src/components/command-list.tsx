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
export const CommandList: React.FC<CommandListProps> = ({ list }: CommandListProps) => {

    const sortedList = list.sort((a, b) => b.tags.sort().join(',').localeCompare(a.tags.sort().join(',')));
    
    console.log(sortedList.map(item => item.tags));

    return (
        <div>
            {ranking.map((rank) => (
                sortedList
                    .filter(item => item.status === rank)
                    .map(item => (
                        <div key={item.command} className="pb-2">
                            <CommandLine
                                command={item}
                            />
                        </div>
                    ))
            ))}
        </div>
    );
};