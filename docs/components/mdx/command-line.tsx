"use client";

import {
    Check,
    CircleAlert,
    CircleCheckBig,
    CircleX,
    HandHelping,
} from "lucide-react";
import type React from "react";
import type { Command } from "@/types/command";

interface CommandLineProps {
    command: Command;
}

export const CommandLine: React.FC<CommandLineProps> = ({ command }) => {
    const commonStyle = "rounded-lg w-full h-[48px] text-black";

    const getCommandLineStyle = (status: string) => {
        const styles: { [key: string]: string } = {
            stable: `${commonStyle} bg-green-500/50 dark:bg-green-300/70`,
            newly: `${commonStyle} bg-blue-500/50 dark:bg-blue-300/70`,
            beta: `${commonStyle} bg-orange-500/50 dark:bg-orange-200/70`,
            proposal: `${commonStyle} bg-gray-500/50 dark:bg-gray-200/70`,
            deprecated: `${commonStyle} bg-red-500/50 dark:bg-red-200/70`,
        };
        return styles[status] || "";
    };

    const getBadgeStyle = (status: string): string => {
        const styles: { [key: string]: string } = {
            proposal: "mr-4 text-gray-700",
            beta: "mr-4 text-orange-800",
            newly: "mr-4 text-blue-800",
            stable: "mr-4 text-green-800",
            deprecated: "mr-4 text-red-800",
        };
        return styles[status] || "";
    };

    // ステータスに対応するアイコンを返す
    const StatusIcon = () => {
        const iconProps = { className: getBadgeStyle(command.status) };
        switch (command.status) {
            case "proposal":
                return <HandHelping {...iconProps} />;
            case "beta":
                return <CircleAlert {...iconProps} />;
            case "newly":
                return <Check {...iconProps} />;
            case "stable":
                return <CircleCheckBig {...iconProps} />;
            case "deprecated":
                return <CircleX {...iconProps} />;
            default:
                return null;
        }
    };

    return (
        <div className={`flex items-center ${getCommandLineStyle(command.status)}`}>
            <div className="flex items-center ml-8">
                <StatusIcon />
                <span className="text-black">{command.command}</span>
                {command.aliases.length > 0 && (
                    <span className="ml-4 text-black/60 text-sm">
                        ({command.aliases.join(", ")})
                    </span>
                )}
            </div>
        </div>
    );
};
