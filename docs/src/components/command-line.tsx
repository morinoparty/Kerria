import React from "react";
import {
    HoverCard,
    HoverCardContent,
    HoverCardTrigger,
} from "@site/src/components/ui/hover-card";
import { CircleCheckBig, Check, CircleAlert, HandHelping, CircleX } from "lucide-react";
import { CommandStatus } from "../types/command";

interface CommandLineProps {
    status: CommandStatus;
    command: string;
    description: string;
    aliases: string[];
    permission: string;
}

// Create the badge component
export const CommandLine: React.FC<CommandLineProps> = ({
    status,
    command,
    description,
    aliases,
    permission,
}) => {
    // Define the common style
    const commonStyle = "rounded-lg max-w-[1024px] h-[48px] text-black"; // Change text color to black

    // Return the style based on the status
    const getCommandLineStyle = (status: string) => {
        const styles: { [key: string]: string } = {
            stable: `${commonStyle} bg-green-500/50 dark:bg-green-300/70`, // Make green more visible
            newly: `${commonStyle} bg-blue-500/50 dark:bg-blue-300/70`, // Make blue more visible
            beta: `${commonStyle} bg-orange-500/50 dark:bg-orange-200/70`, // Make orange more visible
            proposal: `${commonStyle} bg-gray-500/50 dark:bg-gray-200/70`, // Make gray more visible
            deprecated: `${commonStyle} bg-red-500/50 dark:bg-red-200/70`, // Make red more visible
        };
        return styles[status] || "";
    };

    const getBadgeStyle = (status: string): string => {
        const styles: { [key: string]: string } = {
            proposal: "mr-12 text-gray-700", // Light gray
            beta: "mr-12 text-orange-800", // Light orange
            newly: "mr-12 text-blue-800", // Light blue
            stable: "mr-12 text-green-800", // Light green
            deprecated: "mr-12 text-red-800", // Light red
        };
        return styles[status] || "";
    };

    return (
        <>
            <HoverCard>
                <HoverCardTrigger className={getCommandLineStyle(status)}>
                    <div
                        className={`flex items-center ${getCommandLineStyle(status)}`}
                    >
                        <div className="flex items-center ml-8">
                            {status === "proposal" && (
                                <HandHelping className={getBadgeStyle(status)} />
                            )}
                            {status === "beta" && (
                                <CircleAlert className={getBadgeStyle(status)} />
                            )}
                            {status === "newly" && (
                                <Check className={getBadgeStyle(status)} />
                            )}
                            {status === "stable" && (
                                <CircleCheckBig className={getBadgeStyle(status)} />
                            )}
                            {status === "deprecated" && (
                                <CircleX className={getBadgeStyle(status)} />
                            )}
                            <span className="text-black">{command}</span>
                        </div>
                    </div>
                </HoverCardTrigger>
                <HoverCardContent className="text-black bg-white dark:text-white dark:bg-[var(--ifm-background-color)]">
                    <div className="flex flex-col gap-2 text-black dark:text-white">
                        <div className="flex items-center gap-2">
                            <span className="font-medium">
                                {status === "proposal" && "提案中のコマンド"}
                                {status === "beta" && "ベータ版のコマンド"}
                                {status === "newly" && "新しいコマンド"}
                                {status === "stable" && "安定版のコマンド"}
                                {status === "deprecated" && "非推奨のコマンド"}
                            </span>
                        </div>
                        <div className="h-px bg-gray-200 dark:bg-gray-700" />
                        <div className="flex flex-col gap-1">
                            <p className="text-sm">エイリアス: {aliases.join(", ")}</p>
                            <p className="text-sm mb-0">パーミッション: {permission}</p>
                        </div>
                    </div>
                </HoverCardContent>
            </HoverCard>
            <p className="pt-2 pl-8 text-black dark:text-white">説明: {description}</p>
        </>
    );
};