import React from "react";
import {
    HoverCard,
    HoverCardContent,
    HoverCardTrigger,
} from "@site/src/components/ui/hover-card";
import { CircleCheckBig, Check, CircleAlert, HandHelping } from "lucide-react";

// Define the properties for the badge
interface CommandLineProps {
    status: "stable" | "newly" | "beta" | "proposal";
    command: string;
}

// Create the badge component
export const CommandLine: React.FC<CommandLineProps> = ({
                                                            status,
                                                            command,
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
        };
        return styles[status] || "";
    };

    const getBadgeStyle = (status: string): string => {
        const styles: { [key: string]: string } = {
            proposal: "mr-12 text-gray-800", // Light gray
            beta: "mr-12 text-orange-800", // Light orange
            newly: "mr-12 text-blue-800", // Light blue
            stable: "mr-12 text-green-800", // Light green
        };
        return styles[status] || "";
    };

    return (
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
                        <span className="text-black">{command}</span>
                    </div>
                </div>
            </HoverCardTrigger>
            <HoverCardContent className="bg-white dark:bg-[var(--ifm-background-color)] dark:text-white">
                {status === "proposal" && <span>提案中のコマンド</span>}
                {status === "beta" && <span>ベータ版のコマンド</span>}
                {status === "newly" && <span>新しいコマンド</span>}
                {status === "stable" && <span>安定版のコマンド</span>}
            </HoverCardContent>
        </HoverCard>
    );
};