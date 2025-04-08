import React from "react";

const WalkingPusheen = ({visible}) => {
    if (!visible) return null;

    return (
        <>
            <style>{`
        @keyframes walk-back-and-forth {
          0% { right: -150px; }
          100% { right: 100vw; }
        }

        .pusheen-wrapper {
          position: fixed;
          bottom: 50%;
          width: 120px;
          z-index: 9999;
          pointer-events: none;
          animation: walk-back-and-forth 5s linear infinite alternate;
        }

        .pusheen-wrapper img {
          width: 100%;
          height: auto;
          display: block;
          transition: transform 0s;
        }

        .pusheen-wrapper.reverse img {
          transform: scaleX(-1);
        }
      `}</style>

            <div
                className="pusheen-wrapper"
                onAnimationIteration={(e) => {
                    e.currentTarget.classList.toggle("reverse");
                }}
            >
                <img src="/pusheen.gif" alt="Walking cat"/>
            </div>
        </>
    );
};

export default WalkingPusheen;
